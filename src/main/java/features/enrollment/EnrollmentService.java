package features.enrollment;

import models.*;
import models.account.AccountRepository;
import models.account.QuarterlyScheduleRepository;
import models.enums.*;
import models.quarterlysched.QuarterlySchedule;
import models.section.Section;
import models.section.SectionRepository;
import models.student.Student;
import models.subject.Subject;
import models.subject.SubjectRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class EnrollmentService {

    private static final double DOWNPAYMENT = 1000.00;
    private static final double COST_PER_UNIT = 1000.00;
    private static final double FULL_PAYMENT_DISCOUNT = 0.05;
    private static final int MAX_UNITS = 24;

    private EnrollmentService() {}

    // ── GATE CHECK ──────────────────────────────────────────────────────────

    // checks if student has unpaid balance from previous semester
    public static boolean canEnroll(Student student)
            throws IOException, InterruptedException {

        List<Balance> balances = BalanceRepository.getByStudentId(student.getId());

        return balances.stream()
                .noneMatch(b -> b.getRemainingBalance() > 0);
    }

    // ── SUBJECT SELECTION ───────────────────────────────────────────────────

    // returns subjects available for the student to enroll in
    public static List<Subject> getAvailableSubjects(Student student)
            throws IOException, InterruptedException {

        List<Subject> candidates = SubjectRepository.getByCourseAndYearLevel(student.getCourse(), student.getYearLevel());

        List<String> completedCodes = CompletedSubjectRepository
                .getByStudentId(student.getId())
                .stream()
                .map(CompletedSubject::getSubjectCode)
                .collect(Collectors.toList());

        List<String> enrolledCodes = EnrollmentRepository
                .getByStudentId(student.getId())
                .stream()
                .filter(e -> e.getStatus() == Status.ENROLLED
                        || e.getStatus() == Status.PENDING)
                .map(Enrollment::getSubjectCode)
                .collect(Collectors.toList());

        return candidates.stream()
                .filter(s -> !completedCodes.contains(s.getSubjectCode()))
                .filter(s -> !enrolledCodes.contains(s.getSubjectCode()))
                .filter(s -> completedCodes.containsAll(s.getPrerequisites()))
                .collect(Collectors.toList());
    }

    // returns sections available for a specific subject
    public static List<Section> getAvailableSections(String subjectCode)
            throws IOException, InterruptedException {

        return SectionRepository.getBySubjectCode(subjectCode)
                .stream()
                .filter(s -> !s.isFull())
                .collect(Collectors.toList());
    }

    // ── ENROLLMENT ──────────────────────────────────────────────────────────

    public static void enrollSubject(Student student, String sectionId)
            throws IOException, InterruptedException {

        // 1. check enrollment period
        EnrollmentPeriod period = getActivePeriod();
        if (!period.isOpen())
            throw new IllegalStateException("Enrollment is currently closed");

        // 2. check unit limit
        int currentUnits = getCurrentUnits(student.getId());
        Section section = SectionRepository.getById(sectionId);
        if (section == null)
            throw new IllegalArgumentException("Section not found");

        Subject subject = SubjectRepository.getByCode(section.getSubjectCode());
        if (subject == null)
            throw new IllegalArgumentException("Subject not found");

        if (currentUnits + subject.getUnits() > MAX_UNITS)
            throw new IllegalStateException(
                    "Enrolling this subject exceeds the " + MAX_UNITS + " unit limit");

        // 3. check section capacity
        if (section.isFull())
            throw new IllegalStateException("Section is already full");

        // 4. check schedule conflict
        if (hasScheduleConflict(student.getId(), section))
            throw new IllegalStateException(
                    "Schedule conflict with an existing enrollment");

        // 5. create enrollment record
        String id = java.util.UUID.randomUUID().toString();
        Enrollment enrollment = new Enrollment(
                id,
                student.getId(),
                subject.getSubjectCode(),
                sectionId,
                period.getSemester(),
                period.getSchoolYear(),
                Status.PENDING,
                LocalDateTime.now()
        );

        EnrollmentRepository.save(enrollment);

        // 6. increment section count
        section.incrementCurrentCount();
        SectionRepository.update(section);
    }

    // ── CONFIRM ENROLLMENT ──────────────────────────────────────────────────

    // locks subject selection and creates balance + quarterly schedule
    public static void confirmEnrollment(Student student, PaymentPlan paymentPlan)
            throws IOException, InterruptedException {

        EnrollmentPeriod period = getActivePeriod();

        List<Enrollment> pending = EnrollmentRepository
                .getByStudentId(student.getId())
                .stream()
                .filter(e -> e.getStatus() == Status.PENDING)
                .collect(Collectors.toList());

        if (pending.isEmpty())
            throw new IllegalStateException("No pending enrollments to confirm");

        // calculate total cost
        double totalCost = pending.stream()
                .mapToDouble(e -> {
                    try {
                        Subject subject = SubjectRepository
                                .getByCode(e.getSubjectCode());
                        return subject != null ? subject.getUnits() * COST_PER_UNIT : 0;
                    } catch (Exception ex) {
                        return 0;
                    }
                })
                .sum();

        double remainingBalance = totalCost - DOWNPAYMENT;

        // apply 5% discount if full payment
        boolean discountApplied = paymentPlan == PaymentPlan.FULL;
        if (discountApplied)
            remainingBalance = remainingBalance * (1 - FULL_PAYMENT_DISCOUNT);

        // create balance record
        String balanceId = java.util.UUID.randomUUID().toString();
        Balance balance = new Balance(
                balanceId,
                student.getId(),
                period.getSchoolYear(),
                period.getSemester(),
                paymentPlan,
                totalCost,
                false,             // downpayment not yet paid
                remainingBalance,
                discountApplied
        );

        BalanceRepository.save(balance);

        // create quarterly schedule if applicable
        if (paymentPlan == PaymentPlan.QUARTERLY) {
            createQuarterlySchedule(balanceId, remainingBalance);
        }
    }

    // ── DROP SUBJECT ────────────────────────────────────────────────────────

    public static void dropSubject(Student student, String enrollmentId)
            throws IOException, InterruptedException {

        Enrollment enrollment = EnrollmentRepository.getById(enrollmentId);

        if (enrollment == null)
            throw new IllegalArgumentException("Enrollment not found");

        if (enrollment.getStatus() == Status.DROPPED)
            throw new IllegalStateException("Subject is already dropped");

        // update enrollment status
        enrollment.setStatus(Status.DROPPED);
        EnrollmentRepository.update(enrollment);

        // decrement section count
        Section section = SectionRepository.getById(enrollment.getSectionId());
        if (section != null) {
            section.decrementCurrentCount();
            SectionRepository.update(section);
        }

        // flag pending balance on student
        student.setHasPendingBalance(true);
        AccountRepository.update(student);
    }

    // ── HELPERS ─────────────────────────────────────────────────────────────

    private static int getCurrentUnits(String studentId)
            throws IOException, InterruptedException {

        List<Enrollment> active = EnrollmentRepository
                .getByStudentId(studentId)
                .stream()
                .filter(e -> e.getStatus() == Status.ENROLLED
                        || e.getStatus() == Status.PENDING)
                .toList();

        int total = 0;
        for (Enrollment e : active) {
            Subject subject = SubjectRepository.getByCode(e.getSubjectCode());
            if (subject != null) total += subject.getUnits();
        }
        return total;
    }

    private static boolean hasScheduleConflict(String studentId, Section newSection)
            throws IOException, InterruptedException {

        List<Enrollment> active = EnrollmentRepository
                .getByStudentId(studentId)
                .stream()
                .filter(e -> e.getStatus() == Status.ENROLLED
                        || e.getStatus() == Status.PENDING)
                .toList();

        for (Enrollment e : active) {
            Section existing = SectionRepository.getById(e.getSectionId());
            if (existing != null &&
                    existing.getSchedule().equals(newSection.getSchedule())) {
                return true;
            }
        }
        return false;
    }

    private static void createQuarterlySchedule(String balanceId, double remainingBalance)
            throws IOException, InterruptedException {

        Quarter[] quarters = Quarter.values();
        double amountPerQuarter = remainingBalance / quarters.length;

        for (Quarter quarter : quarters) {
            String id = java.util.UUID.randomUUID().toString();
            QuarterlySchedule schedule = new QuarterlySchedule(
                    id,
                    balanceId,
                    quarter,
                    amountPerQuarter,
                    0.0,               // paidAmount starts at 0
                    amountPerQuarter,  // remainingAmount = full quarter amount
                    QuarterStatus.UNPAID
            );
            QuarterlyScheduleRepository.save(schedule);
        }
    }

    private static EnrollmentPeriod getActivePeriod()
            throws IOException, InterruptedException {
        EnrollmentPeriod period = EnrollmentPeriodRepository.getActive();
        if (period == null)
            throw new IllegalStateException("No active enrollment period");
        return period;
    }
}
