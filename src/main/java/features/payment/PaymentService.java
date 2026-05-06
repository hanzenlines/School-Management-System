package features.payment;

import features.enrollment.BalanceRepository;
import features.enrollment.EnrollmentRepository;
import models.Balance;
import models.Enrollment;
import models.Payment;
import models.enums.*;
import models.quarterlysched.QuarterlySchedule;
import models.quarterlysched.QuarterlyScheduleRepository;
import models.student.Student;
import models.subject.SubjectRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PaymentService {

    private PaymentService() {}

    // ── FETCH BALANCE ────────────────────────────────────────────────────────

    public static Balance getCurrentBalance(Student student)
            throws IOException, InterruptedException {

        return BalanceRepository.getByStudentId(student.getId())
                .stream()
                .filter(b -> b.getRemainingBalance() > 0
                        || !b.isDownpaymentPaid())
                .findFirst()
                .orElse(null);
    }

    // ── FETCH QUARTERLY SCHEDULES ────────────────────────────────────────────

    public static List<QuarterlySchedule> getQuarterlySchedules(Balance balance)
            throws IOException, InterruptedException {

        return QuarterlyScheduleRepository.getByBalanceId(balance.getId())
                .stream()
                .sorted(Comparator.comparing(QuarterlySchedule::getQuarter))
                .collect(Collectors.toList());
    }

    // ── FETCH PAYMENT HISTORY ────────────────────────────────────────────────

    public static List<Payment> getPaymentHistory(Student student)
            throws IOException, InterruptedException {

        return PaymentRepository.getByStudentId(student.getId())
                .stream()
                .sorted(Comparator.comparing(Payment::getPaidAt).reversed())
                .collect(Collectors.toList());
    }

    // ── PROCESS PAYMENT ──────────────────────────────────────────────────────

    public static void processPayment(Student student, double amount,
                                      Quarter startingQuarter, PaymentMethod method)
            throws IOException, InterruptedException {

        if (amount <= 0)
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero");

        Balance balance = getCurrentBalance(student);
        if (balance == null)
            throw new IllegalStateException("No outstanding balance found");

        // ── FULL PLAN ──────────────────────────────────────────────────────────

        if (balance.getPaymentPlan() == PaymentPlan.FULL) {
            double required = balance.getRemainingBalance();
            if (amount != required)
                throw new IllegalStateException(
                        "Full payment plan requires the entire balance of "
                                + formatCurrency(required) + " to be paid at once");

            savePayment(student, balance, PaymentType.FULL, null, amount, method);
            balance.setRemainingBalance(0);
            balance.setDownpaymentPaid(true);
            BalanceRepository.update(balance);
            flipToEnrolled(student, balance);
            return;
        }

        // ── QUARTERLY PLAN ─────────────────────────────────────────────────────

        double remaining = amount;

        // handle downpayment first if unpaid
        if (!balance.isDownpaymentPaid()) {
            double downpayment = 1000.00;
            if (remaining < downpayment)
                throw new IllegalStateException(
                        "Minimum payment is the downpayment of "
                                + formatCurrency(downpayment));

            savePayment(student, balance, PaymentType.DOWNPAYMENT, null,
                    downpayment, method);
            balance.setDownpaymentPaid(true);
            balance.setRemainingBalance(balance.getRemainingBalance() - downpayment);
            BalanceRepository.update(balance);
            flipToEnrolled(student, balance);

            remaining -= downpayment;
            if (remaining == 0) return;
        }

        // distribute across quarters starting from selected quarter
        List<QuarterlySchedule> quarters = getQuarterlySchedules(balance);

        // find starting index
        int startIndex = 0;
        if (startingQuarter != null) {
            for (int i = 0; i < quarters.size(); i++) {
                if (quarters.get(i).getQuarter() == startingQuarter) {
                    startIndex = i;
                    break;
                }
            }
        }

        for (int i = startIndex; i < quarters.size(); i++) {
            if (remaining <= 0) break;
            QuarterlySchedule quarter = quarters.get(i);
            if (quarter.getStatus() == QuarterStatus.PAID) continue;

            double due = quarter.getRemainingAmount();

            if (remaining >= due) {
                savePayment(student, balance, PaymentType.QUARTERLY,
                        quarter.getQuarter(), due, method);
                quarter.setPaidAmount(quarter.getPaidAmount() + due);
                quarter.setRemainingAmount(0);
                quarter.setStatus(QuarterStatus.PAID);
                QuarterlyScheduleRepository.update(quarter);
                balance.setRemainingBalance(balance.getRemainingBalance() - due);
                BalanceRepository.update(balance);
                remaining -= due;
            } else {
                savePayment(student, balance, PaymentType.QUARTERLY,
                        quarter.getQuarter(), remaining, method);
                quarter.setPaidAmount(quarter.getPaidAmount() + remaining);
                quarter.setRemainingAmount(quarter.getRemainingAmount() - remaining);
                quarter.setStatus(QuarterStatus.PARTIAL);
                QuarterlyScheduleRepository.update(quarter);
                balance.setRemainingBalance(balance.getRemainingBalance() - remaining);
                BalanceRepository.update(balance);
                remaining = 0;
            }
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private static void flipToEnrolled(Student student, Balance balance)
            throws IOException, InterruptedException {

        List<Enrollment> pending = EnrollmentRepository
                .getByStudentId(student.getId())
                .stream()
                .filter(e -> e.getStatus() == Status.PENDING
                        && e.getSemester() == balance.getSemester()
                        && e.getSchoolYear().equals(balance.getSchoolYear()))
                .collect(Collectors.toList());

        for (Enrollment e : pending) {
            e.setStatus(Status.ENROLLED);
            EnrollmentRepository.update(e);
        }
    }

    private static void savePayment(Student student, Balance balance,
                                    PaymentType type, Quarter quarter,
                                    double amount, PaymentMethod method)
            throws IOException, InterruptedException {

        Payment payment = new Payment(
                UUID.randomUUID().toString(),
                student.getId(),
                balance.getSchoolYear(),
                balance.getSemester(),
                type,
                quarter,
                amount,
                method,
                LocalDateTime.now()
        );
        PaymentRepository.save(payment);
    }

    public static String formatCurrency(double amount) {
        return "PHP " + String.format("%,.2f", amount);
    }
}