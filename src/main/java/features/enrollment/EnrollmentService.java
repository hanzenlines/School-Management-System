package features.enrollment;

import models.*;
import models.account.AccountRepository;
import models.quarterlysched.QuarterlyScheduleRepository;
import models.enums.*;
import models.quarterlysched.QuarterlySchedule;
import models.section.Section;
import models.section.SectionRepository;
import models.student.Student;
import models.subject.CompletedSubject;
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

    public static void canEnroll(Student student)
            throws IOException, InterruptedException {

        EnrollmentPeriod period = EnrollmentPeriodRepository.getActive();
        if (period == null)
            throw new IllegalStateException(
                    "There is no active enrollment period at this time. " +
                            "Please check back later.");

// 1. unpaid balance from a PREVIOUS semester only
        boolean hasUnpaidBalance = BalanceRepository.getByStudentId(student.getId())
                .stream()
                .filter(b -> !(b.getSemester() == period.getSemester()
                        && b.getSchoolYear().equals(period.getSchoolYear())))
                .anyMatch(b -> b.getRemainingBalance() > 0);

        if (hasUnpaidBalance)
            throw new IllegalStateException(
                    "You have an outstanding balance from a previous semester. " +
                            "Please settle your balance before enrolling.");

// 2. already enrolled this semester
        boolean alreadyEnrolled = EnrollmentRepository
                .getByStudentId(student.getId())
                .stream()
                .anyMatch(e -> e.getSemester() == period.getSemester()
                        && e.getSchoolYear().equals(period.getSchoolYear())
                        && (e.getStatus() == Status.ENROLLED
                        || e.getStatus() == Status.PENDING));

        if (alreadyEnrolled)
            throw new IllegalStateException(
                    "You have already enrolled for " + period.getSemester() +
                            " Semester " + period.getSchoolYear() + ".");
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

    // kept for call-site compatibility; just wraps addToCart
    public static void enrollSubject(Student student, Section section)
            throws IOException, InterruptedException {
        addToCart(student, section);
    }

    // ── CONFIRM ENROLLMENT ──────────────────────────────────────────────────

    // locks subject selection and creates balance + quarterly schedule
    public static void confirmEnrollment(Student student, PaymentPlan paymentPlan)
            throws IOException, InterruptedException {

        EnrollmentPeriod period = getActivePeriod();

        List<CartItem> cartItems = getCart(student.getId());
        if (cartItems.isEmpty())
            throw new IllegalStateException("No subjects selected to confirm");

        // 1. save each cart item as a PENDING enrollment + increment section count
        for (CartItem item : cartItems) {
            String id = java.util.UUID.randomUUID().toString();
            Enrollment enrollment = new Enrollment(
                    id,
                    student.getId(),
                    item.subject().getSubjectCode(),
                    item.section().getId(),
                    period.getSemester(),
                    period.getSchoolYear(),
                    Status.PENDING,
                    LocalDateTime.now()
            );
            EnrollmentRepository.save(enrollment);

            item.section().incrementCurrentCount();
            SectionRepository.update(item.section());
        }

        // 2. clear cart now that everything is persisted
        clearCart(student.getId());

        // 3. calculate cost from what was just saved
        List<Enrollment> pending = EnrollmentRepository
                .getByStudentId(student.getId())
                .stream()
                .filter(e -> e.getStatus() == Status.PENDING)
                .collect(Collectors.toList());

        double totalCost = cartItems.stream()
                .mapToDouble(item -> item.subject().getUnits() * COST_PER_UNIT)
                .sum();

        double remainingBalance = totalCost;
        boolean discountApplied = paymentPlan == PaymentPlan.FULL;
        if (discountApplied)
            remainingBalance = remainingBalance * (1 - FULL_PAYMENT_DISCOUNT);

        String balanceId = java.util.UUID.randomUUID().toString();
        Balance balance = new Balance(
                balanceId,
                student.getId(),
                period.getSchoolYear(),
                period.getSemester(),
                paymentPlan,
                totalCost,
                false,
                remainingBalance,
                discountApplied
        );
        BalanceRepository.save(balance);

        if (paymentPlan == PaymentPlan.QUARTERLY)
            createQuarterlySchedule(balanceId, remainingBalance);

        student.setEnrollmentConfirmed(true);
        AccountRepository.update(student);
    }

    // ── DROP SUBJECT ────────────────────────────────────────────────────────

    public static void dropSubject(Student student, Enrollment enrollment)
            throws IOException, InterruptedException {

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

    private static final java.util.Map<String, List<CartItem>> cart = new java.util.concurrent.ConcurrentHashMap<>();

    public record CartItem(Section section, Subject subject) {}

    public static List<CartItem> getCart(String studentId) {
        return cart.getOrDefault(studentId, new java.util.ArrayList<>());
    }

    public static void addToCart(Student student, Section section)
            throws IOException, InterruptedException {

        // 1. check enrollment period
        EnrollmentPeriod period = getActivePeriod();
        if (!period.isOpen())
            throw new IllegalStateException("Enrollment is currently closed");

        Subject subject = SubjectRepository.getByCode(section.getSubjectCode());
        if (subject == null)
            throw new IllegalArgumentException("Subject not found");

        List<CartItem> studentCart = cart.computeIfAbsent(
                student.getId(), k -> new java.util.ArrayList<>());

        // 2. duplicate check
        boolean alreadyInCart = studentCart.stream()
                .anyMatch(item -> item.subject().getSubjectCode()
                        .equals(subject.getSubjectCode()));
        if (alreadyInCart)
            throw new IllegalStateException("Subject already in cart");

        // 3. unit limit check (existing enrolled/pending + cart)
        int currentUnits = getCurrentUnits(student.getId());
        int cartUnits = studentCart.stream()
                .mapToInt(item -> item.subject().getUnits())
                .sum();
        if (currentUnits + cartUnits + subject.getUnits() > MAX_UNITS)
            throw new IllegalStateException(
                    "Adding this subject exceeds the " + MAX_UNITS + " unit limit");

        // 4. section capacity check
        if (section.isFull())
            throw new IllegalStateException("Section is already full");

        // 5. schedule conflict check (against repo + cart)
        if (hasScheduleConflict(student.getId(), section))
            throw new IllegalStateException("Schedule conflict with an existing enrollment");

        boolean cartConflict = studentCart.stream()
                .anyMatch(item -> item.section().getSchedule()
                        .equals(section.getSchedule()));
        if (cartConflict)
            throw new IllegalStateException("Schedule conflict with another selected subject");

        studentCart.add(new CartItem(section, subject));
    }

    public static void removeFromCart(String studentId, String subjectCode) {
        List<CartItem> studentCart = cart.get(studentId);
        if (studentCart != null)
            studentCart.removeIf(item ->
                    item.subject().getSubjectCode().equals(subjectCode));
    }

    public static void clearCart(String studentId) {
        cart.remove(studentId);
    }
}
