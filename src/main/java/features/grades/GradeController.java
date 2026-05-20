package features.grades;

import features.enrollment.EnrollmentRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import models.Enrollment;
import models.Grade;
import models.account.AccountRepository;
import models.enums.Semester;
import models.enums.Status;
import models.faculty.Faculty;
import models.section.Section;
import models.section.SectionRepository;
import models.student.Student;
import models.Subject;
import features.subject.SubjectRepository;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class GradeController {

    @FXML private VBox studentHeader;
    @FXML private VBox semesterGroupsContainer;
    @FXML private Label statusLabel;

    private Student student;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData(Student student) {
        this.student = student;
        renderStudentHeader();
        loadGrades();
    }

    // ── Student Header ────────────────────────────────────────────────────────

    private void renderStudentHeader() {
        HBox row1 = new HBox(10);
//        HBox row2 = new HBox(10);

        row1.getChildren().addAll(
                buildInfoPair("Student Name", student.getName()),
                buildInfoPair("Student No.", student.getId()),
                buildInfoPair("Program", student.getCourse()),
                buildInfoPair("Year Level", String.valueOf(student.getYearLevel()))
        );
//        row2.getChildren().addAll(
//                buildInfoPair("Program", student.getCourse()),
//                buildInfoPair("Year Level", String.valueOf(student.getYearLevel()))
//        );

        studentHeader.getChildren().addAll(row1);
    }

    // ── Load Grades ───────────────────────────────────────────────────────────

    private void loadGrades() {
        new Thread(() -> {
            try {
                // 1. get all enrolled/completed enrollments
                List<Enrollment> enrollments = EnrollmentRepository
                        .getByStudentId(student.getId())
                        .stream()
                        .filter(e -> e.getStatus() == Status.ENROLLED
                                || e.getStatus() == Status.PENDING)
                        .collect(Collectors.toList());

                if (enrollments.isEmpty()) {
                    Platform.runLater(() -> {
                        statusLabel.setText("No enrollment records found.");
                        statusLabel.setVisible(true);
                        statusLabel.setManaged(true);
                    });
                    return;
                }

                // 2. group by semester + schoolYear
                Map<String, List<Enrollment>> grouped = enrollments.stream()
                        .collect(Collectors.groupingBy(
                                e -> e.getSchoolYear() + "|" + e.getSemester()));

                // 3. sort groups by schoolYear desc then semester
                List<String> sortedKeys = grouped.keySet().stream()
                        .sorted(Comparator.reverseOrder())
                        .collect(Collectors.toList());

                // 4. for each group fetch all needed data
                List<SemesterGroup> semesterGroups = new ArrayList<>();

                for (String key : sortedKeys) {
                    List<Enrollment> group = grouped.get(key);
                    List<RowData> rows = new ArrayList<>();

                    for (Enrollment enrollment : group) {
                        Grade grade = GradeRepository.getByStudentAndSection(
                                student.getId(), enrollment.getSectionId());
                        Subject subject = SubjectRepository
                                .getByCode(enrollment.getSubjectCode());
                        Section section = SectionRepository
                                .getById(enrollment.getSectionId());

                        String facultyName = "—";
                        if (section != null && section.getFacultyId() != null) {
                            Faculty faculty = AccountRepository
                                    .getFacultyById(section.getFacultyId());
                            if (faculty != null) facultyName = faculty.getName();
                        }

                        rows.add(new RowData(enrollment, grade, subject,
                                section, facultyName));
                    }

                    semesterGroups.add(new SemesterGroup(
                            group.get(0).getSemester(),
                            group.get(0).getSchoolYear(),
                            group.get(0).getEnrolledAt(),
                            rows));
                }

                Platform.runLater(() -> {
                    statusLabel.setVisible(false);
                    statusLabel.setManaged(false);
                    renderSemesterGroups(semesterGroups);
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        statusLabel.setText("⚠ Failed to load grades."));
                e.printStackTrace();
            }
        }).start();
    }

    // ── Render Semester Groups ────────────────────────────────────────────────

    private void renderSemesterGroups(List<SemesterGroup> groups) {
        semesterGroupsContainer.getChildren().clear();

        for (SemesterGroup group : groups) {
            VBox groupBox = new VBox(0);
            groupBox.setStyle(
                    "-fx-background-color: white; -fx-background-radius: 10; " +
                            "-fx-border-color: #dddcda; -fx-border-radius: 10; -fx-border-width: 1;");

            // subheader
            groupBox.getChildren().add(buildSubheader(group));

            // table header
            groupBox.getChildren().add(buildTableHeader());

            // rows
            for (RowData row : group.rows()) {
                groupBox.getChildren().add(buildGradeRow(row));
            }

            // GWA footer
            groupBox.getChildren().add(buildGwaFooter(group.rows()));

            semesterGroupsContainer.getChildren().add(groupBox);
        }
    }

    // ── Subheader ─────────────────────────────────────────────────────────────

    private HBox buildSubheader(SemesterGroup group) {
        HBox subheader = new HBox(40);
        subheader.setPadding(new Insets(16, 20, 16, 20));
        subheader.setStyle(
                "-fx-border-color: transparent transparent #f0efec transparent; " +
                        "-fx-border-width: 0 0 1 0;");

        subheader.getChildren().addAll(
                buildInfoPair("Academic Year", group.schoolYear()),
                buildInfoPair("Term", group.semester() + " Semester"),
                buildInfoPair("Course", student.getCourse()),
                buildInfoPair("Date Enrolled",
                        group.enrolledAt() != null
                                ? group.enrolledAt().format(DATE_FORMATTER) : "—")
        );

        return subheader;
    }

    // ── Table Header ──────────────────────────────────────────────────────────

    private HBox buildTableHeader() {
        HBox header = new HBox(0);
        header.setPadding(new Insets(8, 20, 8, 20));
        header.setStyle("-fx-background-color: #f5f4f0;");

        header.getChildren().addAll(
                buildHeaderCell("Subject Code", 120),
                buildHeaderCell("Description", 200),
                buildHeaderCell("Section", 80),
                buildHeaderCell("Units", 60),
                buildHeaderCell("Midterm", 90),
                buildHeaderCell("Final", 90),
                buildHeaderCell("Overall", 90),
                buildHeaderCell("Faculty", 150),
                buildHeaderCell("Remarks", 80)
        );

        return header;
    }

    private Label buildHeaderCell(String text, double width) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");
        return label;
    }

    // ── Grade Row ─────────────────────────────────────────────────────────────

    private HBox buildGradeRow(RowData data) {
        HBox row = new HBox(0);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 20, 12, 20));
        row.setStyle(
                "-fx-border-color: transparent transparent #f0efec transparent; " +
                        "-fx-border-width: 0 0 1 0;");

        String subjectCode = data.subject() != null
                ? data.subject().getSubjectCode() : data.enrollment().getSubjectCode();
        String subjectName = data.subject() != null
                ? data.subject().getSubjectName() : "—";
        String sectionId = data.section() != null
                ? data.section().getId() : data.enrollment().getSectionId();
        int units = data.subject() != null ? data.subject().getUnits() : 0;

        String midterm = data.grade() != null
                ? GradeService.formatGrade(data.grade().getComputedMidterm()) : "—";
        String finalGrade = data.grade() != null
                ? GradeService.formatGrade(data.grade().getComputedFinal()) : "—";
        String overall = data.grade() != null
                ? GradeService.formatGrade(data.grade().getOverallGrade()) : "—";
        String remarks = data.grade() != null && data.grade().getOverallGrade() != null
                ? GradeService.getRemarks(data.grade().getOverallGrade()) : "—";

        boolean passed = data.grade() != null
                && data.grade().getOverallGrade() != null
                && data.grade().getOverallGrade() >= 3.0;
        boolean failed = data.grade() != null
                && data.grade().getOverallGrade() != null
                && !passed;

        row.getChildren().addAll(
                buildCell(subjectCode, 120, "#2c2c2a"),
                buildCell(subjectName, 200, "#2c2c2a"),
                buildCell(sectionId, 80, "#888780"),
                buildCell(String.valueOf(units), 60, "#888780"),
                buildCell(midterm, 90, "#2c2c2a"),
                buildCell(finalGrade, 90, "#2c2c2a"),
                buildCell(overall, 90, overall.equals("—") ? "#888780"
                        : (passed ? "#2d6a4f" : "#c0392b")),
                buildCell(data.facultyName(), 150, "#888780"),
                buildCell(remarks, 80, remarks.equals("—") ? "#888780"
                        : (passed ? "#2d6a4f" : "#c0392b"))
        );

        return row;
    }

    private Label buildCell(String text, double width, String color) {
        Label label = new Label(text);
        label.setMinWidth(width);
        label.setWrapText(true);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
        return label;
    }

    // ── GWA Footer ────────────────────────────────────────────────────────────

    private HBox buildGwaFooter(List<RowData> rows) {
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(12, 20, 12, 20));
        footer.setStyle("-fx-background-color: #f5f4f0; " +
                "-fx-background-radius: 0 0 10 10;");

        // weighted GWA — only rows with overallGrade
        double totalWeightedGrade = 0;
        int totalUnits = 0;

        for (RowData row : rows) {
            if (row.grade() != null && row.grade().getOverallGrade() != null
                    && row.subject() != null) {
                int units = row.subject().getUnits();
                totalWeightedGrade += row.grade().getOverallGrade() * units;
                totalUnits += units;
            }
        }

        String gwaText = totalUnits > 0
                ? String.format("%.2f", totalWeightedGrade / totalUnits)
                : "—";

        Label gwaLabel = new Label("GWA: " + gwaText);
        gwaLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        footer.getChildren().add(gwaLabel);
        return footer;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private HBox buildInfoPair(String label, String value) {
        VBox pair = new VBox(2);
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");
        Label valueNode = new Label(value != null ? value : "—");
        valueNode.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c2c2a;");
        pair.getChildren().addAll(labelNode, valueNode);

        HBox box = new HBox(pair);
        HBox.setHgrow(box, Priority.ALWAYS);
        return box;
    }

    // ── Data Records ──────────────────────────────────────────────────────────

    private record SemesterGroup(
            Semester semester,
            String schoolYear,
            java.time.LocalDateTime enrolledAt,
            List<RowData> rows) {}

    private record RowData(
            Enrollment enrollment,
            Grade grade,
            Subject subject,
            Section section,
            String facultyName) {}
}