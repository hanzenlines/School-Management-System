package features.enrollment;

import features.rooms.RoomRepository;
import features.schedule.ScheduleRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import models.Enrollment;
import models.Room;
import models.Schedule;
import models.enums.Status;
import models.Student;
import models.Subject;
import features.subject.SubjectRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class EnrollmentController {

    // ── FXML bindings ─────────────────────────────────────────────────────────

    @FXML private Label enrollmentPeriodLabel;
    @FXML private Label unitCountLabel;
    @FXML private Button tabSubjects;
    @FXML private Button tabSelected;
    @FXML private VBox contentArea;
    @FXML private Button confirmBtn;
    @FXML private Button dropBtn;

    // ── State ─────────────────────────────────────────────────────────────────

    private Student student;
    private boolean enrollmentEligible = false;
    private String enrollmentBlockReason = "";

    private static final String TAB_ACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #2c2c2a; -fx-font-size: 13px; " +
                    "-fx-padding: 10 16; -fx-background-radius: 0; " +
                    "-fx-border-color: transparent transparent #2c2c2a transparent; " +
                    "-fx-border-width: 0 0 2 0; -fx-cursor: hand;";

    private static final String TAB_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #888780; -fx-font-size: 13px; " +
                    "-fx-padding: 10 16; -fx-background-radius: 0; " +
                    "-fx-border-color: transparent; -fx-cursor: hand;";

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData(Student student) {
        this.student = student;
        loadEnrollmentPeriodLabel();
        refreshUnitCount();

        contentArea.getChildren().clear();
        contentArea.getChildren().add(buildLoadingLabel("Checking enrollment eligibility..."));
        confirmBtn.setVisible(false);
        dropBtn.setVisible(false);
        tabSubjects.setDisable(true);
        tabSelected.setDisable(true);

        Thread thread = new Thread(() -> {
            try {
                EnrollmentService.canEnroll(student);
                enrollmentEligible = true;
                Platform.runLater(() -> {
                    tabSubjects.setDisable(false);
                    tabSelected.setDisable(false);
                    showAvailableSubjects();
                });
            } catch (IllegalStateException e) {
                enrollmentEligible = false;
                enrollmentBlockReason = e.getMessage();
                Platform.runLater(() -> {
                    tabSubjects.setDisable(false);
                    tabSelected.setDisable(false);
                    showEnrollmentLocked(enrollmentBlockReason);
                });
            } catch (Exception e) {
                enrollmentEligible = false;
                Platform.runLater(() ->
                        showError("Failed to check enrollment eligibility."));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Tab navigation ────────────────────────────────────────────────────────

    private void showEnrollmentLocked(String reason) {
        contentArea.getChildren().clear();

        VBox messageBox = new VBox(6);
        messageBox.setPadding(new Insets(16, 0, 0, 0));

        Label icon = new Label("⚠ Enrollment Unavailable");
        icon.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        Label reasonLabel = new Label(reason);
        reasonLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #888780;");
        reasonLabel.setWrapText(true);

        messageBox.getChildren().addAll(icon, reasonLabel);
        contentArea.getChildren().add(messageBox);

        confirmBtn.setVisible(false);
        dropBtn.setVisible(false);
    }

    @FXML
    private void showAvailableSubjects() {
        tabSubjects.setStyle(TAB_ACTIVE);
        tabSelected.setStyle(TAB_INACTIVE);
        dropBtn.setVisible(false);
        confirmBtn.setVisible(enrollmentEligible);
        confirmBtn.setManaged(enrollmentEligible);

        if (!enrollmentEligible) {
            showEnrollmentLocked(enrollmentBlockReason);
            return;
        }
        loadAvailableSubjects();
    }

    @FXML
    private void showSelectedSubjects() {
        tabSubjects.setStyle(TAB_INACTIVE);
        tabSelected.setStyle(TAB_ACTIVE);
        confirmBtn.setVisible(enrollmentEligible);
        confirmBtn.setManaged(enrollmentEligible);
        loadSelectedSubjects();
    }

    // ── Load available subjects ───────────────────────────────────────────────

    private void loadAvailableSubjects() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(buildLoadingLabel("Loading subjects..."));

        Thread thread = new Thread(() -> {
            try {
                List<Subject> subjects = EnrollmentService.getAvailableSubjects(student);
                Platform.runLater(() -> renderAvailableSubjects(subjects));
            } catch (Exception e) {
                Platform.runLater(() ->
                        showError("Failed to load subjects: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void renderAvailableSubjects(List<Subject> subjects) {
        contentArea.getChildren().clear();

        if (subjects.isEmpty()) {
            contentArea.getChildren().add(
                    buildEmptyLabel("No available subjects for enrollment."));
            return;
        }

        for (Subject subject : subjects) {
            contentArea.getChildren().add(buildSubjectCard(subject));
        }
    }

    // ── Load selected subjects ────────────────────────────────────────────────

    private void loadSelectedSubjects() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(buildLoadingLabel("Loading your subjects..."));

        Thread thread = new Thread(() -> {
            try {
                List<Enrollment> persisted = EnrollmentRepository
                        .getByStudentId(student.getId())
                        .stream()
                        .filter(e -> e.getStatus() == Status.PENDING
                                || e.getStatus() == Status.ENROLLED)
                        .collect(Collectors.toList());

                List<EnrollmentService.CartItem> cartItems =
                        EnrollmentService.getCart(student.getId());

                Platform.runLater(() -> renderSelectedSubjects(persisted, cartItems));
            } catch (Exception e) {
                Platform.runLater(() ->
                        showError("Failed to load subjects: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void renderSelectedSubjects(List<Enrollment> enrollments,
                                        List<EnrollmentService.CartItem> cartItems) {
        contentArea.getChildren().clear();

        if (enrollments.isEmpty() && cartItems.isEmpty()) {
            contentArea.getChildren().add(buildEmptyLabel("No subjects selected yet."));
            return;
        }

        for (EnrollmentService.CartItem item : cartItems) {
            contentArea.getChildren().add(buildCartItemCard(item));
        }

        for (Enrollment enrollment : enrollments) {
            try {
                Subject subject = SubjectRepository.getByCode(enrollment.getSubjectCode());
                if (subject != null)
                    contentArea.getChildren().add(
                            buildSelectedSubjectCard(enrollment, subject));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ── Section picker ────────────────────────────────────────────────────────

    private void openSectionPicker(Subject subject) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/SectionPickerDialog.fxml"));
            Parent root = loader.load();

            SectionPickerController controller = loader.getController();
            controller.initData(student, subject, () -> {
                refreshUnitCount();
                loadAvailableSubjects();
            });

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

        } catch (IOException e) {
            showError("Could not open section picker: " + e.getMessage());
        }
    }

    // ── Confirm enrollment ────────────────────────────────────────────────────

    @FXML
    private void handleConfirmEnrollment() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/ConfirmEnrollmentDialog.fxml"));
            Parent root = loader.load();

            ConfirmEnrollmentController controller = loader.getController();
            controller.initData(student, () -> {
                refreshUnitCount();
                showSelectedSubjects();
            });

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UNDECORATED);
            dialog.setScene(new Scene(root));
            dialog.showAndWait();

        } catch (IOException e) {
            showError("Could not open confirmation dialog: " + e.getMessage());
        }
    }

    // ── Drop subject ──────────────────────────────────────────────────────────

    @FXML
    private void handleDrop() {}

    private void dropEnrollment(Enrollment enrollment) {
        Thread thread = new Thread(() -> {
            try {
                EnrollmentService.dropSubject(student, enrollment);
                Platform.runLater(() -> {
                    refreshUnitCount();
                    loadSelectedSubjects();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Drop failed: " + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Unit count ────────────────────────────────────────────────────────────

    private void refreshUnitCount() {
        Thread thread = new Thread(() -> {
            try {
                List<Enrollment> active = EnrollmentRepository
                        .getByStudentId(student.getId())
                        .stream()
                        .filter(e -> e.getStatus() == Status.ENROLLED
                                || e.getStatus() == Status.PENDING)
                        .collect(Collectors.toList());

                int units = 0;
                for (Enrollment e : active) {
                    Subject subject = SubjectRepository.getByCode(e.getSubjectCode());
                    if (subject != null) units += subject.getUnits();
                }

                int cartUnits = EnrollmentService.getCart(student.getId())
                        .stream()
                        .mapToInt(item -> item.subject().getUnits())
                        .sum();

                final int total = units + cartUnits;
                Platform.runLater(() ->
                        unitCountLabel.setText(total + " / 24 units"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Enrollment period label ───────────────────────────────────────────────

    private void loadEnrollmentPeriodLabel() {
        Thread thread = new Thread(() -> {
            try {
                var period = EnrollmentPeriodRepository.getActive();
                if (period != null) {
                    String text = period.getSemester() + " Semester · "
                            + period.getSchoolYear();
                    Platform.runLater(() -> enrollmentPeriodLabel.setText(text));
                } else {
                    Platform.runLater(() ->
                            enrollmentPeriodLabel.setText("No active enrollment period"));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Card builders ─────────────────────────────────────────────────────────

    private VBox buildSubjectCard(Subject subject) {
        VBox card = new VBox(4);
        card.setPadding(new Insets(14, 16, 14, 16));

        String baseStyle =
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-border-color: #dddcda; -fx-border-radius: 8; -fx-border-width: 1;" +
                        (student.isEnrollmentConfirmed() ? "" : " -fx-cursor: hand;");
        card.setStyle(baseStyle);

        HBox top = new HBox();
        Label nameLabel = new Label(subject.getSubjectName());
        nameLabel.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");
        HBox.setHgrow(nameLabel, Priority.ALWAYS);

        Label unitLabel = new Label(subject.getUnits() + " units");
        unitLabel.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #888780; " +
                        "-fx-background-color: #f0efec; -fx-background-radius: 10; -fx-padding: 2 8;");

        top.getChildren().addAll(nameLabel, unitLabel);

        Label codeLabel = new Label(subject.getSubjectCode());
        codeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        card.getChildren().addAll(top, codeLabel);

        if (!student.isEnrollmentConfirmed()) {
            card.setOnMouseClicked(e -> openSectionPicker(subject));
            card.setOnMouseEntered(e -> card.setStyle(
                    "-fx-background-color: #f8f7f5; -fx-background-radius: 8; " +
                            "-fx-border-color: #b0afac; -fx-border-radius: 8; -fx-border-width: 1; " +
                            "-fx-cursor: hand;"));
            card.setOnMouseExited(e -> card.setStyle(baseStyle));
        }

        return card;
    }

    private HBox buildCartItemCard(EnrollmentService.CartItem item) {
        HBox card = new HBox(12);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: #f0f7ff; -fx-background-radius: 8; " +
                        "-fx-border-color: #b3d4f5; -fx-border-radius: 8; -fx-border-width: 1;");
        card.setPadding(new Insets(14, 16, 14, 16));

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(item.subject().getSubjectName());
        nameLabel.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        String scheduleDisplay = resolveSchedules(item.section().getScheduleIds());

        Label codeLabel = new Label(item.subject().getSubjectCode()
                + "  ·  " + item.subject().getUnits() + " units"
                + "  ·  " + scheduleDisplay);
        codeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        info.getChildren().addAll(nameLabel, codeLabel);

        Label badge = new Label("Selected");
        badge.setStyle(
                "-fx-background-color: #cce5ff; -fx-text-fill: #004085; " +
                        "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px;");

        Button removeBtn = new Button("Remove");
        removeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #888780; " +
                        "-fx-font-size: 12px; -fx-padding: 4 10; -fx-border-color: #888780; " +
                        "-fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;");
        removeBtn.setOnAction(e -> {
            EnrollmentService.removeFromCart(student.getId(),
                    item.subject().getSubjectCode());
            refreshUnitCount();
            loadSelectedSubjects();
        });

        card.getChildren().addAll(info, badge, removeBtn);
        return card;
    }

    private HBox buildSelectedSubjectCard(Enrollment enrollment, Subject subject) {
        HBox card = new HBox(12);
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-border-color: #dddcda; -fx-border-radius: 8; -fx-border-width: 1;");
        card.setPadding(new Insets(14, 16, 14, 16));

        VBox info = new VBox(4);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLabel = new Label(subject.getSubjectName());
        nameLabel.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        Label codeLabel = new Label(subject.getSubjectCode()
                + "  ·  " + subject.getUnits() + " units");
        codeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        info.getChildren().addAll(nameLabel, codeLabel);

        String badgeStyle = enrollment.getStatus() == Status.ENROLLED
                ? "-fx-background-color: #d4edda; -fx-text-fill: #2d6a4f; " +
                "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px;"
                : "-fx-background-color: #fff3cd; -fx-text-fill: #856404; " +
                "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px;";
        Label statusBadge = new Label(enrollment.getStatus().toString());
        statusBadge.setStyle(badgeStyle);

        Button dropBtn = new Button("Drop");
        dropBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #c0392b; " +
                        "-fx-font-size: 12px; -fx-padding: 4 10; -fx-border-color: #c0392b; " +
                        "-fx-border-radius: 6; -fx-border-width: 1; -fx-cursor: hand;");
        dropBtn.setOnAction(e -> dropEnrollment(enrollment));

        card.getChildren().addAll(info, statusBadge, dropBtn);
        return card;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private Label buildLoadingLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #888780;");
        label.setPadding(new Insets(16, 0, 0, 0));
        return label;
    }

    private Label buildEmptyLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #888780;");
        label.setPadding(new Insets(16, 0, 0, 0));
        return label;
    }

    private void showError(String message) {
        contentArea.getChildren().clear();
        Label label = new Label("⚠ " + message);
        label.setStyle("-fx-font-size: 13px; -fx-text-fill: #c0392b;");
        label.setPadding(new Insets(16, 0, 0, 0));
        contentArea.getChildren().add(label);
    }

    private String resolveSchedules(List<String> scheduleIds) {
        if (scheduleIds == null || scheduleIds.isEmpty()) return "No schedule";
        try {
            StringBuilder sb = new StringBuilder();
            for (String scheduleId : scheduleIds) {
                Schedule s = ScheduleRepository.getById(scheduleId);
                if (s == null) continue;
                Room r = RoomRepository.getById(s.getRoomId());
                String roomName = r != null ? r.getRoomName() : "Unknown Room";
                if (sb.length() > 0) sb.append(" | ");
                sb.append(s.getTimeSlot()).append(" @ ").append(roomName);
            }
            return sb.isEmpty() ? "—" : sb.toString();
        } catch (Exception e) {
            return "—";
        }
    }
}