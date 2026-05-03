package features.enrollment;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Enrollment;
import models.enums.PaymentPlan;
import models.enums.Status;
import models.student.Student;
import models.subject.Subject;
import models.subject.SubjectRepository;

import java.util.List;

public class ConfirmEnrollmentController {

    @FXML private Label totalCostLabel;
    @FXML private Label remainingLabel;

    @FXML private ToggleGroup paymentPlanGroup;
    @FXML private RadioButton quarterlyRadio;
    @FXML private RadioButton fullRadio;

    @FXML private VBox quarterlyOption;
    @FXML private VBox fullOption;

    private Student student;
    private Runnable onSuccess;

    private double totalCost = 0;

    public void initData(Student student, Runnable onSuccess) {
        this.student = student;
        this.onSuccess = onSuccess;

        loadSummary();
        setupSelectionUI();
    }

    // ── Load cost summary ─────────────────────────────────────────────

    private void loadSummary() {
        Thread thread = new Thread(() -> {
            try {
//                List<Enrollment> enrollments =
//                        EnrollmentRepository.getByStudentId(student.getId());

                List<EnrollmentService.CartItem> cartItems = EnrollmentService.getCart(student.getId());

//                double total = enrollments.stream()
//                        .filter(e -> e.getStatus() == Status.PENDING)
//                        .mapToDouble(e -> {
//                            try {
//                                Subject subject =
//                                        SubjectRepository.getByCode(e.getSubjectCode());
//                                return subject != null ? subject.getUnits() * 1000 : 0;
//                            } catch (Exception ex) {
//                                return 0;
//                            }
//                        })
//                        .sum();

                double total = cartItems.stream()
                        .mapToDouble(item -> {
                            Subject subject = item.subject();
                            return subject != null ? subject.getUnits() * 1000 : 0;
                        })
                        .sum();

                totalCost = total;
//                double remaining = total - 1000;

                Platform.runLater(() -> {
                    totalCostLabel.setText(formatCurrency(total));
//                    remainingLabel.setText(formatCurrency(remaining));
                });

            } catch (Exception ignored) {}
        });

        thread.setDaemon(true);
        thread.start();
    }

    // ── UI selection behavior ─────────────────────────────────────────

    private void setupSelectionUI() {
        paymentPlanGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            updateStyles();
        });

        quarterlyOption.setOnMouseClicked(e -> quarterlyRadio.setSelected(true));
        fullOption.setOnMouseClicked(e -> fullRadio.setSelected(true));

        updateStyles();
    }

    private void updateStyles() {
        boolean isQuarterly = quarterlyRadio.isSelected();

        quarterlyOption.setStyle(isQuarterly ? selectedStyle() : unselectedStyle());
        fullOption.setStyle(!isQuarterly ? selectedStyle() : unselectedStyle());
    }

    private String selectedStyle() {
        return "-fx-background-color: white; -fx-background-radius: 8;" +
                "-fx-border-color: #2c2c2a; -fx-border-width: 1.5; -fx-border-radius: 8;";
    }

    private String unselectedStyle() {
        return "-fx-background-color: white; -fx-background-radius: 8;" +
                "-fx-border-color: #dddcda; -fx-border-width: 1.5; -fx-border-radius: 8;";
    }

    // ── Confirm action ────────────────────────────────────────────────

    @FXML
    private void handleConfirm() {
        Thread thread = new Thread(() -> {
            try {
                PaymentPlan plan = getSelectedPaymentPlan();

                EnrollmentService.confirmEnrollment(student, plan);

                Platform.runLater(() -> {
                    if (onSuccess != null) onSuccess.run();
                    close();
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        totalCostLabel.setText("Error: " + e.getMessage()));
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private PaymentPlan getSelectedPaymentPlan() {
        return fullRadio.isSelected()
                ? PaymentPlan.FULL
                : PaymentPlan.QUARTERLY;
    }

    // ── Utility ───────────────────────────────────────────────────────

    private String formatCurrency(double amount) {
        return "PHP " + String.format("%,.2f", amount);
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) totalCostLabel.getScene().getWindow();
        stage.close();
    }
}