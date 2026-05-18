package features.grades;

import models.Grade;
import models.GradingPeriod;
import models.section.Section;
import models.enums.Semester;
import models.section.Section;
import models.student.Student;
import models.subject.CompletedSubject;
import models.subject.CompletedSubjectRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

public class GradeService {

    private static final double PASSING_GRADE = 3.0;

    private GradeService() {}

    // ── CAN EDIT ─────────────────────────────────────────────────────────────

    public static boolean canEdit()
            throws IOException, InterruptedException {

        GradingPeriod period = GradingPeriodRepository.getActive();
        if (period == null) return false;
        if (!period.isOpen()) return false;

        // past deadline — flag but don't block (still returns true)
        // the UI will show a warning if past deadline
        return true;
    }

    public static boolean isPastDeadline()
            throws IOException, InterruptedException {

        GradingPeriod period = GradingPeriodRepository.getActive();
        if (period == null) return false;
        return LocalDateTime.now().isAfter(period.getDeadline());
    }

    // ── SUBMIT / UPDATE GRADE ─────────────────────────────────────────────────

    public static void saveGrade(String studentId, String sectionId,
                                 String subjectCode, Semester semester,
                                 String schoolYear,
                                 Double prelim, Double midterm,
                                 Double preFinal, Double finalGrade)
            throws IOException, InterruptedException {

        if (!canEdit())
            throw new IllegalStateException("Grading period is closed");

        // check if grade record already exists
        Grade existing = GradeRepository.getByStudentAndSection(studentId, sectionId);

        // compute what we can
        Double computedMidterm = computeMidterm(prelim, midterm);
        Double computedFinal = computeFinal(preFinal, finalGrade);
        Double overall = computeOverall(computedMidterm, computedFinal);

        if (existing == null) {
            // create new grade record
            Grade grade = new Grade(
                    UUID.randomUUID().toString(),
                    studentId, sectionId, subjectCode,
                    semester, schoolYear,
                    prelim, midterm, preFinal, finalGrade,
                    computedMidterm, computedFinal, overall,
                    LocalDateTime.now()
            );
            GradeRepository.save(grade);
            if (overall != null && overall >= PASSING_GRADE) {
                boolean alreadyCompleted = CompletedSubjectRepository
                        .existsByStudentAndSubject(studentId, subjectCode);
                if (!alreadyCompleted) {
                    CompletedSubject completed = new CompletedSubject(
                            UUID.randomUUID().toString(),
                            studentId,
                            subjectCode,
                            overall,
                            semester,
                            schoolYear
                    );
                    CompletedSubjectRepository.save(completed);
                }
            }
        } else {
            // update existing — only overwrite non-null incoming values
            if (prelim != null) existing.setPrelimGrade(prelim);
            if (midterm != null) existing.setMidtermGrade(midterm);
            if (preFinal != null) existing.setPreFinalGrade(preFinal);
            if (finalGrade != null) existing.setFinalGrade(finalGrade);

            // recompute
            Double recomputedMidterm = computeMidterm(
                    existing.getPrelimGrade(), existing.getMidtermGrade());
            Double recomputedFinal = computeFinal(
                    existing.getPreFinalGrade(), existing.getFinalGrade());
            Double recomputedOverall = computeOverall(recomputedMidterm, recomputedFinal);

            existing.setComputedMidterm(recomputedMidterm);
            existing.setComputedFinal(recomputedFinal);
            existing.setOverallGrade(recomputedOverall);
            existing.setSubmittedAt(LocalDateTime.now());

            // check if overall is now passing and not already completed
            if (overall != null && overall >= PASSING_GRADE) {
                boolean alreadyCompleted = CompletedSubjectRepository
                        .existsByStudentAndSubject(studentId, subjectCode);

                if (!alreadyCompleted) {
                    CompletedSubject completed = new CompletedSubject(
                            java.util.UUID.randomUUID().toString(),
                            studentId,
                            subjectCode,
                            overall,
                            semester,
                            schoolYear
                    );
                    CompletedSubjectRepository.save(completed);
                }
            }

            GradeRepository.update(existing);
        }
    }

    // ── REMARKS ───────────────────────────────────────────────────────────────

    public static String getRemarks(Double overallGrade) {
        if (overallGrade == null) return "—";
        return overallGrade >= PASSING_GRADE ? "Passed" : "Failed";
    }

    // ── COMPUTATION ───────────────────────────────────────────────────────────

    public static Double computeMidterm(Double prelim, Double midterm) {
        if (prelim == null || midterm == null) return null;
        return (prelim * 0.40) + (midterm * 0.60);
    }

    public static Double computeFinal(Double preFinal, Double finalGrade) {
        if (preFinal == null || finalGrade == null) return null;
        return (preFinal * 0.40) + (finalGrade * 0.60);
    }

    public static Double computeOverall(Double computedMidterm, Double computedFinal) {
        if (computedMidterm == null || computedFinal == null) return null;
        return (computedMidterm + computedFinal) / 2.0;
    }

    public static String formatGrade(Double grade) {
        if (grade == null) return "—";
        return String.format("%.2f", grade);
    }
}