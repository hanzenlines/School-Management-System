package features.evaluation;

import features.enrollment.EnrollmentPeriodRepository;
import features.enrollment.EnrollmentRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Enrollment;
import models.EnrollmentPeriod;
import models.FacultyEvaluation;
import models.Faculty;
import features.faculty.FacultyRepository;
import models.enums.Status;
import models.Section;
import features.section.SectionRepository;
import models.Student;
import models.Subject;
import features.subject.SubjectRepository;

import java.util.*;
import java.util.stream.Collectors;

public class EvaluationController {

    @FXML private VBox contentArea;

    private Student student;

    // fixed criteria in display order
    private static final List<String[]> CRITERIA = List.of(
            new String[]{"teachingEffectiveness", "Teaching Effectiveness",
                    "Explains lessons clearly and thoroughly"},
            new String[]{"subjectKnowledge", "Subject Knowledge",
                    "Demonstrates mastery of the subject matter"},
            new String[]{"communication", "Communication",
                    "Approachable and responsive to student concerns"},
            new String[]{"professionalism", "Professionalism",
                    "Punctual, prepared, and organized"},
            new String[]{"studentEngagement", "Student Engagement",
                    "Encourages participation and provides feedback"}
    );

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData(Student student) {
        this.student = student;
        loadFacultyList();
    }

    // ── Load faculty list ─────────────────────────────────────────────────────

    private void loadFacultyList() {
        contentArea.getChildren().clear();

        new Thread(() -> {
            try {
                // get active enrollment period for semester/schoolYear context
                EnrollmentPeriod period = EnrollmentPeriodRepository.getActive();

                // get all enrolled sections
                List<Enrollment> enrollments = EnrollmentRepository
                        .getByStudentId(student.getId())
                        .stream()
                        .filter(e -> e.getStatus() == Status.ENROLLED)
                        .collect(Collectors.toList());

                if (enrollments.isEmpty()) {
                    Platform.runLater(() -> {
                        Label empty = new Label(
                                "You have no enrolled subjects to evaluate.");
                        empty.setStyle(
                                "-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                    });
                    return;
                }

                // build list of EvalEntry (one per section/faculty)
                List<EvalEntry> entries = new ArrayList<>();
                Set<String> seen = new HashSet<>(); // dedupe facultyId+sectionId

                for (Enrollment enrollment : enrollments) {
                    Section section = SectionRepository
                            .getById(enrollment.getSectionId());
                    if (section == null) continue;

                    String key = section.getFacultyId() + "|" + section.getId();
                    if (seen.contains(key)) continue;
                    seen.add(key);

                    Faculty faculty = FacultyRepository
                            .getById(section.getFacultyId());
                    if (faculty == null) continue;

                    Subject subject = SubjectRepository
                            .getByCode(section.getSubjectCode());
                    String subjectName = subject != null
                            ? subject.getSubjectName() : section.getSubjectCode();

                    // check if already evaluated
                    boolean alreadyDone = period != null &&
                            EvaluationRepository
                                    .existsByStudentFacultyAndSection(
                                            student.getId(),
                                            faculty.getId(),
                                            section.getId(),
                                            period.getSemester());

                    entries.add(new EvalEntry(
                            faculty, section, subjectName,
                            alreadyDone, period));
                }

                Platform.runLater(() -> {
                    if (entries.isEmpty()) {
                        Label empty = new Label("No faculty to evaluate.");
                        empty.setStyle(
                                "-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                        return;
                    }
                    for (EvalEntry entry : entries) {
                        contentArea.getChildren().add(buildFacultyCard(entry));
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load faculty list.");
                    error.setStyle(
                            "-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(error);
                });
                e.printStackTrace();
            }
        }).start();
    }

    // ── Faculty card ──────────────────────────────────────────────────────────

    private VBox buildFacultyCard(EvalEntry entry) {
        VBox card = new VBox(6);

        String base =
                "-fx-background-color: white; -fx-border-color: #e0ded8; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16;"
                        + (entry.alreadyDone() ? "" : " -fx-cursor: hand;");
        String hover =
                "-fx-background-color: #fafaf8; -fx-border-color: #c8c6c0; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16; -fx-cursor: hand;";

        card.setStyle(base);
        if (!entry.alreadyDone()) {
            card.setOnMouseEntered(e -> card.setStyle(hover));
            card.setOnMouseExited(e -> card.setStyle(base));
            card.setOnMouseClicked(e -> openEvalModal(entry));
        }

        // ── Meta row ──────────────────────────────────────────────────────────
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label(
                entry.section().getSectionCode()
                        + "  ·  " + entry.subjectName());
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");
        metaRow.getChildren().add(meta);

        if (entry.alreadyDone()) {
            Label badge = new Label("✓ EVALUATED");
            badge.setStyle(
                    "-fx-background-color: #e6f4ea; -fx-text-fill: #2d7a3a; " +
                            "-fx-font-size: 10px; -fx-background-radius: 4; " +
                            "-fx-padding: 2 6;");
            metaRow.getChildren().add(badge);
        }

        Label nameLabel = new Label(entry.faculty().getName());
        nameLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        Label deptLabel = new Label(entry.faculty().getDepartment());
        deptLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        card.getChildren().addAll(metaRow, nameLabel, deptLabel);
        return card;
    }

    // ── Evaluation modal ──────────────────────────────────────────────────────

    private void openEvalModal(EvalEntry entry) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setResizable(false);

        VBox form = new VBox(20);
        form.setPadding(new Insets(28));
        form.setPrefWidth(500);

        Label heading = new Label("Evaluate " + entry.faculty().getName());
        heading.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");

        Label subheading = new Label(
                entry.subjectName() + "  ·  " + entry.section().getSectionCode());
        subheading.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        Label notice = new Label(
                "Your evaluation is anonymous. Ratings are from 1 (lowest) to 5 (highest).");
        notice.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: #888780; " +
                        "-fx-background-color: #f0ede6; -fx-background-radius: 6; " +
                        "-fx-padding: 8 10;");
        notice.setWrapText(true);

        form.getChildren().addAll(heading, subheading, notice);

        // ── Criteria rows with radio buttons ──────────────────────────────────
        Map<String, ToggleGroup> toggleGroups = new LinkedHashMap<>();

        for (String[] criterion : CRITERIA) {
            String key         = criterion[0];
            String label       = criterion[1];
            String description = criterion[2];

            VBox criterionBox = new VBox(6);

            Label criterionLabel = new Label(label);
            criterionLabel.setStyle(
                    "-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

            Label descLabel = new Label(description);
            descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

            // radio buttons 1–5
            ToggleGroup group = new ToggleGroup();
            HBox radioRow = new HBox(16);
            radioRow.setAlignment(Pos.CENTER_LEFT);

            for (int i = 1; i <= 5; i++) {
                VBox radioBox = new VBox(4);
                radioBox.setAlignment(Pos.CENTER);

                RadioButton rb = new RadioButton();
                rb.setToggleGroup(group);
                rb.setUserData(i);
                rb.setStyle("-fx-cursor: hand;");

                Label scoreLabel = new Label(String.valueOf(i));
                scoreLabel.setStyle(
                        "-fx-font-size: 11px; -fx-text-fill: #888780;");

                radioBox.getChildren().addAll(rb, scoreLabel);
                radioRow.getChildren().add(radioBox);
            }

            toggleGroups.put(key, group);
            criterionBox.getChildren().addAll(
                    criterionLabel, descLabel, radioRow);

            // separator
            Separator sep = new Separator();
            sep.setStyle("-fx-background-color: #e0ded8;");

            form.getChildren().addAll(criterionBox, sep);
        }

        // ── Comments field ────────────────────────────────────────────────────
        Label commentsLabel = new Label("Additional Comments (optional)");
        commentsLabel.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        TextArea commentsField = new TextArea();
        commentsField.setPromptText(
                "Share any additional feedback about this faculty member...");
        commentsField.setWrapText(true);
        commentsField.setPrefRowCount(4);
        commentsField.setStyle(
                "-fx-background-color: #fafaf8; -fx-border-color: #e0ded8; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-font-size: 13px;");

        form.getChildren().addAll(commentsLabel, commentsField);

        // ── Actions ───────────────────────────────────────────────────────────
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #888780; " +
                        "-fx-font-size: 13px; -fx-border-color: #e0ded8; -fx-border-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                        "-fx-padding: 7 16; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> modal.close());

        Button submitBtn = new Button("Submit Evaluation");
        submitBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-background-radius: 6; " +
                        "-fx-padding: 7 16; -fx-cursor: hand;");

        submitBtn.setOnAction(e -> {
            // validate all criteria rated
            for (String[] criterion : CRITERIA) {
                String key = criterion[0];
                String label = criterion[1];
                if (toggleGroups.get(key).getSelectedToggle() == null) {
                    showError(modal, "Please rate all criteria. Missing: " + label);
                    return;
                }
            }

            int te = (int) toggleGroups.get("teachingEffectiveness")
                    .getSelectedToggle().getUserData();
            int sk = (int) toggleGroups.get("subjectKnowledge")
                    .getSelectedToggle().getUserData();
            int co = (int) toggleGroups.get("communication")
                    .getSelectedToggle().getUserData();
            int pr = (int) toggleGroups.get("professionalism")
                    .getSelectedToggle().getUserData();
            int se = (int) toggleGroups.get("studentEngagement")
                    .getSelectedToggle().getUserData();
            String comments = commentsField.getText().trim();

            new Thread(() -> {
                try {
                    FacultyEvaluation eval = new FacultyEvaluation(
                            UUID.randomUUID().toString(),
                            student.getId(),
                            entry.faculty().getId(),
                            entry.section().getId(),
                            entry.period() != null
                                    ? entry.period().getSemester() : null,
                            entry.period() != null
                                    ? entry.period().getSchoolYear() : "",
                            te, sk, co, pr, se,
                            comments
                    );
                    EvaluationRepository.save(eval);
                    Platform.runLater(() -> {
                        modal.close();
                        loadFacultyList();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() ->
                            showError(modal, "Failed to submit evaluation."));
                }
            }).start();
        });

        actions.getChildren().addAll(cancelBtn, submitBtn);
        form.getChildren().add(actions);

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");

        modal.setScene(new Scene(scroll, 520, 640));
        modal.showAndWait();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private void showError(Stage owner, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if (owner != null) alert.initOwner(owner);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ── Entry record ──────────────────────────────────────────────────────────

    private record EvalEntry(
            Faculty faculty,
            Section section,
            String subjectName,
            boolean alreadyDone,
            EnrollmentPeriod period
    ) {}
}