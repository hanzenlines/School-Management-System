package features.payment;

import features.enrollment.BalanceRepository;
import features.enrollment.EnrollmentPeriodRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import models.*;
import models.enums.*;
import models.quarterlysched.QuarterlySchedule;
import models.student.Student;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaymentController {

    // ── FXML ─────────────────────────────────────────────────────────────────

    @FXML private Label periodLabel;

    // statement
    @FXML private Label totalCostLabel;
    @FXML private Label remainingBalanceLabel;
    @FXML private Label paymentPlanLabel;
    @FXML private Label downpaymentStatusLabel;
    @FXML private VBox quarterlySection;
    @FXML private VBox quarterRowsContainer;
    @FXML private VBox statementCard;

    // form
    @FXML private TextField studentIdField;
    @FXML private TextField studentNameField;
    @FXML private TextField emailField;
    @FXML private TextField courseField;
    @FXML private TextField termField;
    @FXML private TextField schoolYearField;
    @FXML private ComboBox<String> paymentForCombo;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private TextField amountField;
    @FXML private Label errorLabel;

    // history
    @FXML private VBox historyContainer;

    // ── State ─────────────────────────────────────────────────────────────────

    private Student student;
    private Balance currentBalance;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData(Student student) {
        this.student = student;

        // fill static fields immediately
        studentIdField.setText(student.getId());
        studentNameField.setText(student.getName());
        emailField.setText(student.getEmail());
        courseField.setText(student.getCourse());

        // fill payment method combo
        paymentMethodCombo.getItems().addAll("Cash", "GCash", "PayMaya", "Debit Card");

        loadAll();
    }

    // ── Load everything ───────────────────────────────────────────────────────

    private void loadAll() {
        Thread thread = new Thread(() -> {
            try {
                Balance balance = PaymentService.getCurrentBalance(student);
                List<Payment> history = PaymentService.getPaymentHistory(student);

                EnrollmentPeriod period = EnrollmentPeriodRepository.getActive();

                List<QuarterlySchedule> quarters = (balance != null
                        && balance.getPaymentPlan() == PaymentPlan.QUARTERLY)
                        ? PaymentService.getQuarterlySchedules(balance)
                        : List.of();

                Platform.runLater(() -> {
                    currentBalance = balance;

                    // period label
                    if (period != null) {
                        periodLabel.setText(period.getSemester() + " Semester · "
                                + period.getSchoolYear());
                        termField.setText(period.getSemester().toString());
                        schoolYearField.setText(period.getSchoolYear());
                    }

                    renderStatement(balance, quarters);
                    populatePaymentForCombo(balance, quarters);
                    renderHistory(history);
                });

            } catch (Exception e) {
                Platform.runLater(() -> showError("Failed to load payment data."));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    // ── Statement ─────────────────────────────────────────────────────────────

    private void renderStatement(Balance balance, List<QuarterlySchedule> quarters) {
        if (balance == null) {
            totalCostLabel.setText("—");
            remainingBalanceLabel.setText("—");
            paymentPlanLabel.setText("—");
            downpaymentStatusLabel.setText("—");
            return;
        }

        // add downpayment row if not yet paid
        if (!balance.isDownpaymentPaid()) {
            HBox dpRow = new HBox(12);
            dpRow.setAlignment(Pos.CENTER_LEFT);
            dpRow.setPadding(new Insets(12, 16, 12, 16));
            dpRow.setStyle(
                    "-fx-background-color: #fffbf0; -fx-background-radius: 8; " +
                            "-fx-border-color: #f0e68c; -fx-border-radius: 8; -fx-border-width: 1;");

            VBox dpInfo = new VBox(2);
            HBox.setHgrow(dpInfo, Priority.ALWAYS);

            Label dpTitle = new Label("Downpayment");
            dpTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

            Label dpAmount = new Label(PaymentService.formatCurrency(1000.00) + " required to confirm enrollment");
            dpAmount.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

            dpInfo.getChildren().addAll(dpTitle, dpAmount);

            Label dpBadge = new Label("Unpaid");
            dpBadge.setStyle(
                    "-fx-background-color: #fff3cd; -fx-text-fill: #856404; " +
                            "-fx-background-radius: 10; -fx-padding: 3 10; -fx-font-size: 11px;");

            Button dpPayBtn = new Button("PAY");
            dpPayBtn.setStyle(
                    "-fx-background-color: #2c2c2a; -fx-text-fill: white; " +
                            "-fx-font-size: 11px; -fx-background-radius: 4; " +
                            "-fx-padding: 4 10; -fx-cursor: hand;");
            dpPayBtn.setOnAction(e -> {
                paymentForCombo.getSelectionModel().select("Downpayment");
                amountField.setText("1000");
                amountField.requestFocus();
            });

            dpRow.getChildren().addAll(dpInfo, dpBadge, dpPayBtn);

            // insert after the summary HBox — find the parent VBox and add it
            // since we're inside renderStatement which has access to the card VBox,
            // just add it to quarterlySection's parent by injecting into the statement card
            statementCard.getChildren().add(dpRow);
        }

        totalCostLabel.setText(PaymentService.formatCurrency(balance.getTotalCost()));
        remainingBalanceLabel.setText(
                PaymentService.formatCurrency(balance.getRemainingBalance()));
        paymentPlanLabel.setText(balance.getPaymentPlan().toString());
        downpaymentStatusLabel.setText(balance.isDownpaymentPaid() ? "✓ Paid" : "Unpaid");
        downpaymentStatusLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: "
                        + (balance.isDownpaymentPaid() ? "#2d6a4f;" : "#c0392b;"));

        if (balance.getPaymentPlan() == PaymentPlan.QUARTERLY && !quarters.isEmpty()) {
            quarterlySection.setVisible(true);
            quarterlySection.setManaged(true);
            renderQuarterRows(quarters);
        }
    }

    private void renderQuarterRows(List<QuarterlySchedule> quarters) {
        quarterRowsContainer.getChildren().clear();

        for (QuarterlySchedule q : quarters) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 16, 10, 16));
            row.setStyle("-fx-border-color: transparent transparent #f0efec transparent; " +
                    "-fx-border-width: 0 0 1 0;");

            Label quarterLabel = new Label(q.getQuarter().toString());
            quarterLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c2c2a; -fx-pref-width: 120;");
            // remove the HBox.setHgrow(quarterLabel, Priority.ALWAYS) line
            HBox spacer = new HBox();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label scheduled = new Label(
                    PaymentService.formatCurrency(q.getScheduledAmount()));
            scheduled.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c2c2a; -fx-pref-width: 120;");

            Label paid = new Label(PaymentService.formatCurrency(q.getPaidAmount()));
            paid.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c2c2a; -fx-pref-width: 120;");

            Label balanceAmt = new Label(
                    PaymentService.formatCurrency(q.getRemainingAmount()));
            boolean hasBalance = q.getRemainingAmount() > 0;
            balanceAmt.setStyle("-fx-font-size: 13px; -fx-pref-width: 120; -fx-text-fill: "
                    + (hasBalance ? "#c0392b;" : "#2d6a4f;"));

            // PAY button — only if not fully paid
            HBox actionBox = new HBox();
            actionBox.setPrefWidth(60);
            actionBox.setAlignment(Pos.CENTER_RIGHT);

            if (q.getStatus() != QuarterStatus.PAID) {
                Button payBtn = new Button("PAY");
                payBtn.setStyle(
                        "-fx-background-color: #2c2c2a; -fx-text-fill: white; " +
                                "-fx-font-size: 11px; -fx-background-radius: 4; " +
                                "-fx-padding: 4 10; -fx-cursor: hand;");
                payBtn.setOnAction(e -> preselectQuarter(q.getQuarter()));
                actionBox.getChildren().add(payBtn);
            }

            row.getChildren().addAll(quarterLabel, spacer, scheduled, paid, balanceAmt, actionBox);
            quarterRowsContainer.getChildren().add(row);
        }
    }

    // ── Payment For combo ─────────────────────────────────────────────────────

    private void populatePaymentForCombo(Balance balance,
                                         List<QuarterlySchedule> quarters) {
        paymentForCombo.getItems().clear();

        if (balance == null) return;

        if (!balance.isDownpaymentPaid()) {
            paymentForCombo.getItems().add("Downpayment");
        }

        if (balance.getPaymentPlan() == PaymentPlan.QUARTERLY) {
            for (QuarterlySchedule q : quarters) {
                if (q.getStatus() != QuarterStatus.PAID) {
                    paymentForCombo.getItems().add(q.getQuarter().toString());
                }
            }
        } else if (balance.getPaymentPlan() == PaymentPlan.FULL
                && !balance.isDownpaymentPaid()) {
            paymentForCombo.getItems().add("Full Payment");
        }

        if (!paymentForCombo.getItems().isEmpty()) {
            paymentForCombo.getSelectionModel().selectFirst();
        }
    }

    private void preselectQuarter(Quarter quarter) {
        // scroll to form and preselect the quarter
        paymentForCombo.getSelectionModel().select(quarter.toString());
        amountField.requestFocus();
    }

    // ── Handle Pay Now ────────────────────────────────────────────────────────

    @FXML
    private void handlePayNow() {
        hideError();

        if (currentBalance == null) {
            showError("No outstanding balance found.");
            return;
        }

        String amountText = amountField.getText().trim();
        if (amountText.isEmpty()) {
            showError("Please enter an amount.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showError("Please enter a valid amount.");
            return;
        }

        String paymentFor = paymentForCombo.getValue();
        String methodStr = paymentMethodCombo.getValue();

        if (paymentFor == null || paymentFor.isEmpty()) {
            showError("Please select what you are paying for.");
            return;
        }

        if (methodStr == null || methodStr.isEmpty()) {
            showError("Please select a payment method.");
            return;
        }

        // resolve starting quarter
        Quarter startingQuarter = resolveStartingQuarter(paymentFor);

        // resolve payment method
        PaymentMethod method = switch (methodStr) {
            case "GCash" -> PaymentMethod.GCASH;
            case "PayMaya" -> PaymentMethod.PAYMAYA;
            case "Debit Card" -> PaymentMethod.DEBIT_CARD;
            default -> PaymentMethod.CASH;
        };

        Thread thread = new Thread(() -> {
            try {
                PaymentService.processPayment(student, amount, startingQuarter, method);
                Platform.runLater(() -> {
                    amountField.clear();
                    loadAll(); // refresh everything
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError(e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private Quarter resolveStartingQuarter(String paymentFor) {
        return switch (paymentFor) {
            case "Q1" -> Quarter.PRELIM;
            case "Q2" -> Quarter.MIDTERM;
            case "Q3" -> Quarter.PREFINAL;
            case "Q4" -> Quarter.FINAL;
            default -> null; // downpayment or full payment
        };
    }

    // ── Payment History ───────────────────────────────────────────────────────

    private void renderHistory(List<Payment> payments) {
        historyContainer.getChildren().clear();

        if (payments.isEmpty()) {
            Label empty = new Label("No payments recorded yet.");
            empty.setStyle("-fx-font-size: 13px; -fx-text-fill: #888780;");
            empty.setPadding(new Insets(8, 0, 0, 0));
            historyContainer.getChildren().add(empty);
            return;
        }

        for (Payment p : payments) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(10, 16, 10, 16));
            row.setStyle("-fx-border-color: transparent transparent #f0efec transparent; " +
                    "-fx-border-width: 0 0 1 0;");

            Label date = new Label(p.getPaidAt().format(FORMATTER));
            date.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c2c2a; -fx-pref-width: 160;");

            Label type = new Label(p.getType().toString());
            type.setStyle("-fx-font-size: 12px; -fx-text-fill: #2c2c2a; -fx-pref-width: 120;");

            Label quarter = new Label(p.getQuarter() != null
                    ? p.getQuarter().toString() : "—");
            quarter.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780; -fx-pref-width: 100;");

            Label method = new Label(p.getPaymentMethod() != null
                    ? p.getPaymentMethod().toString() : "—");
            method.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780; -fx-pref-width: 120;");

            Label amount = new Label(PaymentService.formatCurrency(p.getAmount()));
            amount.setStyle("-fx-font-size: 12px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");
            HBox.setHgrow(amount, Priority.ALWAYS);

            row.getChildren().addAll(date, type, quarter, method, amount);
            historyContainer.getChildren().add(row);
        }
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private void showError(String message) {
        errorLabel.setText("⚠ " + message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void hideError() {
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}