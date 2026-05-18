package features.sections;

import features.grades.GradeRepository;
import features.grades.GradeService;
import features.grades.GradingPeriodRepository;
import features.enrollment.EnrollmentRepository;
import features.rooms.RoomRepository;
import features.schedule.ScheduleRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.*;
import models.account.AccountRepository;
import models.enums.Status;
import models.faculty.Faculty;
import models.faculty.FacultyController;
import models.section.Section;
import models.student.Student;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class SectionDetailController {

    @FXML private Label sectionTitleLabel;
    @FXML private Label sectionDetailLabel;
    @FXML private Label deadlineLabel;
    @FXML private Label emptyLabel;
    @FXML private VBox studentRows;

    private Faculty faculty;
    private Section section;
    private boolean pastDeadline;
    private Runnable onBack;

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData(Faculty faculty, Section section,
                         boolean pastDeadline, Runnable onBack) {
        this.faculty = faculty;
        this.section = section;
        this.pastDeadline = pastDeadline;
        this.onBack = onBack;

        sectionTitleLabel.setText("Section " + section.getId());
        sectionDetailLabel.setText(section.getSubjectCode()); // placeholder while loading

        new Thread(() -> {
            try {
                List<String> scheduleIds = section.getScheduleIds();
                StringBuilder sb = new StringBuilder();

                for (String sid : scheduleIds) {
                    Schedule s = ScheduleRepository.getById(sid);
                    if (s == null) continue;
                    Room r = RoomRepository.getById(s.getRoomId());
                    String roomName = r != null ? r.getRoomName() : "Unknown Room";
                    if (sb.length() > 0) sb.append(" | ");
                    sb.append(s.getTimeSlot()).append(" @ ").append(roomName);
                }

                String display = sb.isEmpty() ? "No schedule" : sb.toString();
                Platform.runLater(() ->
                        sectionDetailLabel.setText(section.getSubjectCode()
                                + "  ·  " + display));
            } catch (Exception e) {
                Platform.runLater(() ->
                        sectionDetailLabel.setText(section.getSubjectCode()));
            }
        }).start();

        loadDeadlineLabel();
        loadStudents();
    }

    private void loadDeadlineLabel() {
        new Thread(() -> {
            try {
                GradingPeriod period = GradingPeriodRepository.getActive();
                Platform.runLater(() -> {
                    if (period == null) {
                        deadlineLabel.setText("No active grading period");
                        return;
                    }
                    String formatted = period.getDeadline()
                            .format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
                    if (pastDeadline) {
                        deadlineLabel.setText("⚠ Deadline passed: " + formatted);
                        deadlineLabel.setStyle(
                                "-fx-font-size: 12px; -fx-text-fill: #c0392b;");
                    } else {
                        deadlineLabel.setText("Deadline: " + formatted);
                        deadlineLabel.setStyle(
                                "-fx-font-size: 12px; -fx-text-fill: #888780;");
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // ── Load Students ─────────────────────────────────────────────────────────

    private void loadStudents() {
        studentRows.getChildren().clear();

        new Thread(() -> {
            try {
                // get enrolled students for this section
                List<Enrollment> enrollments = EnrollmentRepository
                        .getBySectionId(section.getId())
                        .stream()
                        .filter(e -> e.getStatus() == Status.ENROLLED)
                        .collect(Collectors.toList());

                // get existing grades for this section
                List<Grade> grades = GradeRepository.getBySectionId(section.getId());

                Platform.runLater(() -> {
                    if (enrollments.isEmpty()) {
                        emptyLabel.setVisible(true);
                        emptyLabel.setManaged(true);
                        return;
                    }

                    for (Enrollment enrollment : enrollments) {
                        // find existing grade if any
                        Grade existingGrade = grades.stream()
                                .filter(g -> g.getStudentId()
                                        .equals(enrollment.getStudentId()))
                                .findFirst()
                                .orElse(null);

                        studentRows.getChildren().add(
                                buildStudentRow(enrollment, existingGrade));
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label err = new Label("⚠ Failed to load students.");
                    err.setStyle("-fx-font-size: 13px; -fx-text-fill: #c0392b;");
                    studentRows.getChildren().add(err);
                });
                e.printStackTrace();
            }
        }).start();
    }

    // ── Student Row ───────────────────────────────────────────────────────────

    private HBox buildStudentRow(Enrollment enrollment, Grade grade) {
        HBox row = new HBox(16.5); // Gap between Prelim, Midterm, Prefinal, Final Fields
        row.setAlignment(Pos.CENTER_LEFT);
//        row.setPadding(new Insets(10, 16, 10, 16));
        row.setStyle("-fx-background-color: white; " +
                "-fx-border-color: transparent transparent #f0efec transparent; " +
                "-fx-border-width: 0 0 1 0;");

        // student name — load async
        VBox studentInfo = new VBox(5);
        studentInfo.setPadding(new Insets(5, 0, 5, 5)); // top/bottom padding

        studentInfo.setMinWidth(101.5);

//        HBox.setHgrow(studentInfo, Priority.ALWAYS);
        Label studentNameLabel = new Label("Loading...");
        studentNameLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c2c2a;");
        Label studentIdLabel = new Label(enrollment.getStudentId());
        studentIdLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");
        studentInfo.getChildren().addAll(studentNameLabel, studentIdLabel);

        new Thread(() -> {
            try {
                Student student = AccountRepository.getStudentById(enrollment.getStudentId());
                if (student != null) {
                    Platform.runLater(() -> studentNameLabel.setText(student.getName()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        // grade input fields — disabled if past deadline
        TextField prelimField = buildGradeField(
                grade != null ? grade.getPrelimGrade() : null);
        TextField midtermField = buildGradeField(
                grade != null ? grade.getMidtermGrade() : null);
        TextField preFinalField = buildGradeField(
                grade != null ? grade.getPreFinalGrade() : null);
        TextField finalField = buildGradeField(
                grade != null ? grade.getFinalGrade() : null);

        if (pastDeadline) {
            prelimField.setDisable(true);
            midtermField.setDisable(true);
            preFinalField.setDisable(true);
            finalField.setDisable(true);
        }

        // computed display labels
        // Adjust width values para ma ilisan ang gap sa Computed Grades
        Label computedMidLabel = buildComputedLabel(
                grade != null ? grade.getComputedMidterm() : null, 30);
        Label computedFinalLabel = buildComputedLabel(
                grade != null ? grade.getComputedFinal() : null, 30);
        Label overallLabel = buildComputedLabel(
                grade != null ? grade.getOverallGrade() : null, 30);

        // save button
        Button saveBtn = new Button("Save");
        saveBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white; " +
                        "-fx-font-size: 11px; -fx-background-radius: 4; " +
                        "-fx-padding: 4 12; -fx-cursor: hand;");
        saveBtn.setVisible(!pastDeadline);
        saveBtn.setManaged(!pastDeadline);

        saveBtn.setOnAction(e -> handleSave(
                enrollment, grade,
                prelimField, midtermField, preFinalField, finalField,
                computedMidLabel, computedFinalLabel, overallLabel,
                saveBtn));

        HBox.setMargin(computedMidLabel, new Insets(0, 0, 0, 40));
        HBox.setMargin(computedFinalLabel, new Insets(0, 0, 0, 55));
        HBox.setMargin(overallLabel, new Insets(0, 55, 0, 32));

        row.getChildren().addAll(
                studentInfo,
                prelimField, midtermField, preFinalField, finalField,
                computedMidLabel, computedFinalLabel, overallLabel,
                saveBtn);

        return row;
    }

    private TextField buildGradeField(Double value) {
        TextField field = new TextField(value != null
                ? GradeService.formatGrade(value) : "");
        field.setPromptText("—");
        field.setPrefWidth(70  ); // kalapdon sa fields
        field.setStyle(
                "-fx-font-size: 12px; -fx-background-color: #f5f4f0; " +
                        "-fx-background-radius: 4; -fx-border-color: #dddcda; " +
                        "-fx-border-radius: 4; -fx-padding: 4 8;");
        return field;
    }

    private Label buildComputedLabel(Double value, double width) {
        Label label = new Label(GradeService.formatGrade(value));
        String color = value != null
                ? (value >= 3.0 ? "#2d6a4f" : "#c0392b")
                : "#888780";
        label.setMinWidth(width);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + ";");
        return label;
    }

    // ── Save Grade ────────────────────────────────────────────────────────────

    private void handleSave(Enrollment enrollment, Grade existingGrade,
                            TextField prelimField, TextField midtermField,
                            TextField preFinalField, TextField finalField,
                            Label computedMidLabel, Label computedFinalLabel,
                            Label overallLabel, Button saveBtn) {

        Double prelim = parseGrade(prelimField.getText());
        Double midterm = parseGrade(midtermField.getText());
        Double preFinal = parseGrade(preFinalField.getText());
        Double finalGrade = parseGrade(finalField.getText());

        // validate ranges
        try {
            validateGrade(prelim, "Prelim");
            validateGrade(midterm, "Midterm");
            validateGrade(preFinal, "Pre-Final");
            validateGrade(finalGrade, "Final");
        } catch (IllegalArgumentException e) {
            showRowError(e.getMessage());
            return;
        }

        saveBtn.setDisable(true);
        saveBtn.setText("Saving...");

        new Thread(() -> {
            try {
                GradeService.saveGrade(
                        enrollment.getStudentId(),
                        section.getId(),
                        section.getSubjectCode(),
                        enrollment.getSemester(),
                        enrollment.getSchoolYear(),
                        prelim, midterm, preFinal, finalGrade);

                // recompute for display
                Double compMid = GradeService.computeMidterm(prelim, midterm);
                Double compFinal = GradeService.computeFinal(preFinal, finalGrade);
                Double overall = GradeService.computeOverall(compMid, compFinal);

                Platform.runLater(() -> {
                    updateComputedLabel(computedMidLabel, compMid);
                    updateComputedLabel(computedFinalLabel, compFinal);
                    updateComputedLabel(overallLabel, overall);
                    saveBtn.setDisable(false);
                    saveBtn.setText("Save");
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showRowError("Save failed: " + e.getMessage());
                    saveBtn.setDisable(false);
                    saveBtn.setText("Save");
                });
            }
        }).start();
    }

    private void updateComputedLabel(Label label, Double value) {
        label.setText(GradeService.formatGrade(value));
        if (value != null) {
            label.setStyle("-fx-font-size: 12px; -fx-text-fill: "
                    + (value >= 3.0 ? "#2d6a4f;" : "#c0392b;")
                    + " -fx-pref-width: 80;");
        }
    }

    private Double parseGrade(String text) {
        if (text == null || text.isBlank() || text.equals("—")) return null;
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void validateGrade(Double grade, String fieldName) {
        if (grade != null && (grade < 1.0 || grade > 5.0))
            throw new IllegalArgumentException(
                    fieldName + " grade must be between 1.0 and 5.0");
    }

    private void showRowError(String message) {
        // show a temporary error snackbar at the top of the list
        Label err = new Label("⚠ " + message);
        err.setStyle("-fx-font-size: 12px; -fx-text-fill: #c0392b; " +
                "-fx-background-color: #fdecea; -fx-background-radius: 6; " +
                "-fx-padding: 6 12;");
        studentRows.getChildren().add(0, err);

        // auto-remove after 3 seconds
        new Thread(() -> {
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
            Platform.runLater(() -> studentRows.getChildren().remove(err));
        }).start();
    }

    // ── Back ──────────────────────────────────────────────────────────────────

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/faculty_dashboard.fxml"));
            Scene scene = new Scene(loader.load());

            FacultyController controller = loader.getController();
            controller.initData(faculty);
            controller.navigateTo("sections");

            Stage stage = (Stage) sectionTitleLabel.getScene().getWindow();
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}