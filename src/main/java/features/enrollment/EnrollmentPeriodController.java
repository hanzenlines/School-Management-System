package features.enrollment;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.EnrollmentPeriod;
import models.enums.Semester;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EnrollmentPeriodController {

    @FXML private VBox contentArea;

    private static final DateTimeFormatter DISPLAY_FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
    private static final DateTimeFormatter INPUT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData() {
        loadPeriod();
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private void loadPeriod() {
        contentArea.getChildren().clear();

        new Thread(() -> {
            try {
                EnrollmentPeriod active = EnrollmentPeriodRepository.getActive();
                List<EnrollmentPeriod> all = EnrollmentPeriodRepository.getAll();

                Platform.runLater(() -> {
                    // ── Active period card ────────────────────────────────────
                    if (active != null) {
                        contentArea.getChildren().add(buildActivePeriodCard(active));
                    } else {
                        Label none = new Label("No enrollment period is currently open.");
                        none.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(none);
                    }

                    // ── New period button ─────────────────────────────────────
                    Button newBtn = new Button("+ New Enrollment Period");
                    newBtn.setStyle(
                            "-fx-background-color: #2c2c2a; -fx-text-fill: white;" +
                                    "-fx-font-size: 13px; -fx-background-radius: 6;" +
                                    "-fx-padding: 8 16; -fx-cursor: hand;");
                    newBtn.setOnAction(e -> openEditModal(null));

                    HBox toolbar = new HBox(newBtn);
                    toolbar.setAlignment(Pos.CENTER_RIGHT);
                    contentArea.getChildren().add(toolbar);

                    // ── All periods list ──────────────────────────────────────
                    if (all.size() > 1 || (all.size() == 1 && active == null)) {
                        Label historyLabel = new Label("All Periods");
                        historyLabel.setStyle(
                                "-fx-font-size: 12px; -fx-text-fill: #888780; " +
                                        "-fx-font-weight: 500;");
                        contentArea.getChildren().add(historyLabel);

                        for (EnrollmentPeriod p : all) {
                            if (active != null && p.getId().equals(active.getId())) continue;
                            contentArea.getChildren().add(buildPeriodCard(p));
                        }
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load enrollment period.");
                    error.setStyle("-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(error);
                });
            }
        }).start();
    }

    // ── Active period card ────────────────────────────────────────────────────

    private VBox buildActivePeriodCard(EnrollmentPeriod period) {
        VBox card = new VBox(12);
        card.setStyle(
                "-fx-background-color: white; -fx-border-color: #e0ded8; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 20;");

        // ── Status badge + heading row ────────────────────────────────────────
        Label statusBadge = new Label(period.isOpen() ? "OPEN" : "CLOSED");
        statusBadge.setStyle(period.isOpen()
                ? "-fx-background-color: #e6f4ea; -fx-text-fill: #2d7a3a; " +
                "-fx-font-size: 11px; -fx-background-radius: 4; -fx-padding: 3 8;"
                : "-fx-background-color: #f0ede6; -fx-text-fill: #888780; " +
                "-fx-font-size: 11px; -fx-background-radius: 4; -fx-padding: 3 8;");

        Label heading = new Label(period.getSemester() + " Semester  ·  " + period.getSchoolYear());
        heading.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");

        HBox headingRow = new HBox(10, heading, statusBadge);
        headingRow.setAlignment(Pos.CENTER_LEFT);

        // ── Date range ────────────────────────────────────────────────────────
        Label dateRange = new Label(
                period.getStart().format(DISPLAY_FMT)
                        + "  →  " + period.getEnd().format(DISPLAY_FMT));
        dateRange.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        // ── Action buttons ────────────────────────────────────────────────────
        Button toggleBtn = new Button(period.isOpen() ? "Close Enrollment" : "Open Enrollment");
        toggleBtn.setStyle(period.isOpen()
                ? "-fx-background-color: transparent; -fx-text-fill: #a32d2d; " +
                "-fx-font-size: 13px; -fx-border-color: #e8c8c8; -fx-border-radius: 6; " +
                "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                "-fx-padding: 7 16; -fx-cursor: hand;"
                : "-fx-background-color: transparent; -fx-text-fill: #2d7a3a; " +
                "-fx-font-size: 13px; -fx-border-color: #b8dfc0; -fx-border-radius: 6; " +
                "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                "-fx-padding: 7 16; -fx-cursor: hand;");
        toggleBtn.setOnAction(e -> handleToggle(period));

        Button editBtn = new Button("Edit");
        editBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #888780; " +
                        "-fx-font-size: 13px; -fx-border-color: #e0ded8; -fx-border-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                        "-fx-padding: 7 16; -fx-cursor: hand;");
        editBtn.setOnAction(e -> openEditModal(period));

        HBox btnRow = new HBox(10, toggleBtn, editBtn);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(headingRow, dateRange, btnRow);
        return card;
    }

    // ── Non-active period card ────────────────────────────────────────────────

    private VBox buildPeriodCard(EnrollmentPeriod period) {
        VBox card = new VBox(6);
        String base =
                "-fx-background-color: white; -fx-border-color: #e0ded8; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16; -fx-cursor: hand;";
        String hover =
                "-fx-background-color: #fafaf8; -fx-border-color: #c8c6c0; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16; -fx-cursor: hand;";

        card.setStyle(base);
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e -> card.setStyle(base));
        card.setOnMouseClicked(e -> openEditModal(period));

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label(period.getSemester() + " Semester  ·  " + period.getSchoolYear());
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

        Label badge = new Label(period.isOpen() ? "OPEN" : "CLOSED");
        badge.setStyle(period.isOpen()
                ? "-fx-background-color: #e6f4ea; -fx-text-fill: #2d7a3a; " +
                "-fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 2 6;"
                : "-fx-background-color: #f0ede6; -fx-text-fill: #888780; " +
                "-fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 2 6;");

        metaRow.getChildren().addAll(meta, badge);

        Label dateRange = new Label(
                period.getStart().format(DISPLAY_FMT)
                        + "  →  " + period.getEnd().format(DISPLAY_FMT));
        dateRange.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c2c2a;");

        card.getChildren().addAll(metaRow, dateRange);
        return card;
    }

    // ── Toggle open/close ─────────────────────────────────────────────────────

    private void handleToggle(EnrollmentPeriod period) {
        String action = period.isOpen() ? "close" : "open";
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to " + action + " enrollment?");
        confirm.showAndWait().filter(b -> b == ButtonType.OK).ifPresent(b -> {
            new Thread(() -> {
                try {
                    period.setOpen(!period.isOpen());
                    EnrollmentPeriodRepository.update(period);
                    Platform.runLater(this::loadPeriod);
                } catch (Exception e) {
                    Platform.runLater(() -> showError(null, "Failed to update enrollment period."));
                }
            }).start();
        });
    }

    // ── Edit / Create modal ───────────────────────────────────────────────────

    private void openEditModal(EnrollmentPeriod period) {
        boolean isNew = period == null;

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setResizable(false);

        // ── Fields ────────────────────────────────────────────────────────────
        ComboBox<Semester> semesterBox = new ComboBox<>();
        semesterBox.getItems().addAll(Semester.values());
        semesterBox.setValue(isNew ? Semester.values()[0] : period.getSemester());
        styleComboBox(semesterBox);

        TextField schoolYearField = new TextField(isNew ? "" : period.getSchoolYear());
        schoolYearField.setPromptText("e.g. 2025-2026");
        styleTextField(schoolYearField);

        TextField startField = new TextField(
                isNew ? "" : period.getStart().format(INPUT_FMT));
        startField.setPromptText("yyyy-MM-dd HH:mm");
        styleTextField(startField);

        TextField endField = new TextField(
                isNew ? "" : period.getEnd().format(INPUT_FMT));
        endField.setPromptText("yyyy-MM-dd HH:mm");
        styleTextField(endField);

        // ── Form layout ───────────────────────────────────────────────────────
        VBox form = new VBox(14);
        form.setPadding(new Insets(28));
        form.setPrefWidth(440);

        Label heading = new Label(isNew ? "New Enrollment Period" : "Edit Enrollment Period");
        heading.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");

        form.getChildren().addAll(
                heading,
                labeledField("Semester", semesterBox),
                labeledField("School Year", schoolYearField),
                labeledField("Start  (yyyy-MM-dd HH:mm)", startField),
                labeledField("End  (yyyy-MM-dd HH:mm)", endField)
        );

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

        Button saveBtn = new Button(isNew ? "Create" : "Save Changes");
        saveBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-background-radius: 6; " +
                        "-fx-padding: 7 16; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            String schoolYear = schoolYearField.getText().trim();
            String startText  = startField.getText().trim();
            String endText    = endField.getText().trim();

            if (schoolYear.isEmpty() || startText.isEmpty() || endText.isEmpty()) {
                showError(modal, "All fields are required.");
                return;
            }

            LocalDateTime start, end;
            try {
                start = LocalDateTime.parse(startText, INPUT_FMT);
                end   = LocalDateTime.parse(endText, INPUT_FMT);
            } catch (Exception ex) {
                showError(modal, "Invalid date format. Use yyyy-MM-dd HH:mm");
                return;
            }

            if (!end.isAfter(start)) {
                showError(modal, "End date must be after start date.");
                return;
            }

            new Thread(() -> {
                try {
                    if (isNew) {
                        EnrollmentPeriod newPeriod = new EnrollmentPeriod(
                                "EP-" + System.currentTimeMillis(),
                                semesterBox.getValue(),
                                schoolYear, start, end, false);
                        EnrollmentPeriodRepository.save(newPeriod);
                    } else {
                        EnrollmentPeriod updated = new EnrollmentPeriod(
                                period.getId(),
                                semesterBox.getValue(),
                                schoolYear, start, end, period.isOpen());
                        EnrollmentPeriodRepository.update(updated);
                    }
                    Platform.runLater(() -> {
                        modal.close();
                        loadPeriod();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() -> showError(modal, "Failed to save enrollment period."));
                }
            }).start();
        });

        actions.getChildren().addAll(cancelBtn, saveBtn);
        form.getChildren().add(actions);

        modal.setScene(new Scene(form, Color.WHITE));
        modal.showAndWait();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private VBox labeledField(String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #888780; -fx-font-weight: 500;");
        if (field instanceof ComboBox<?> cb) cb.setMaxWidth(Double.MAX_VALUE);
        return new VBox(5, label, field);
    }

    private void styleTextField(TextField f) {
        f.setStyle(
                "-fx-background-color: #fafaf8; -fx-border-color: #e0ded8; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-padding: 8 10; -fx-font-size: 13px;");
    }

    private <T> void styleComboBox(ComboBox<T> cb) {
        cb.setStyle(
                "-fx-background-color: #fafaf8; -fx-border-color: #e0ded8; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-font-size: 13px;");
        cb.setMaxWidth(Double.MAX_VALUE);
    }

    private void showError(Stage owner, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if (owner != null) alert.initOwner(owner);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}