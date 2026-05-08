package features.schedule;

import features.rooms.RoomRepository;
import features.schedule.ScheduleRepository;
import features.schedule.ScheduleService;
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
import models.Room;
import models.Schedule;

import java.util.List;

public class ScheduleController {

    @FXML private VBox contentArea;

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData() {
        loadRoomList();
    }

    // ── Level 1: Room list ────────────────────────────────────────────────────

    private void loadRoomList() {
        contentArea.getChildren().clear();

        new Thread(() -> {
            try {
                List<Room> rooms = RoomRepository.getAll();

                Platform.runLater(() -> {
                    if (rooms.isEmpty()) {
                        Label empty = new Label("No rooms found. Add rooms first.");
                        empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                        return;
                    }
                    for (Room r : rooms) {
                        contentArea.getChildren().add(buildRoomCard(r));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load rooms.");
                    error.setStyle("-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(error);
                });
            }
        }).start();
    }

    private VBox buildRoomCard(Room room) {
        VBox card = new VBox(6);

        String baseStyle =
                "-fx-background-color: white; -fx-border-color: #e0ded8; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16; -fx-cursor: hand;";
        String hoverStyle =
                "-fx-background-color: #fafaf8; -fx-border-color: #c8c6c0; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16; -fx-cursor: hand;";

        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));
        card.setOnMouseClicked(e -> loadRoomSchedules(room));

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label(room.getRoomType().toString()
                + " · Capacity: " + room.getCapacity());
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

        metaRow.getChildren().add(meta);

        if (!room.isActive()) {
            Label badge = new Label("INACTIVE");
            badge.setStyle(
                    "-fx-background-color: #f0ede6; -fx-text-fill: #888780; " +
                            "-fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 2 6;");
            metaRow.getChildren().add(badge);
        }

        Label nameLabel = new Label(room.getRoomName());
        nameLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        card.getChildren().addAll(metaRow, nameLabel);
        return card;
    }

    // ── Level 2: Schedules for a room ─────────────────────────────────────────

    private void loadRoomSchedules(Room room) {
        contentArea.getChildren().clear();

        // ── Toolbar: back + room name + add button ────────────────────────────
        Button backBtn = new Button("← Rooms");
        backBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #888780; " +
                        "-fx-font-size: 13px; -fx-border-color: #e0ded8; -fx-border-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                        "-fx-padding: 7 14; -fx-cursor: hand;");
        backBtn.setOnAction(e -> loadRoomList());

        Label roomTitle = new Label(room.getRoomName());
        roomTitle.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");

        Button addBtn = new Button("+ New Schedule");
        addBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        addBtn.setOnAction(e -> openScheduleModal(null, room));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(10, backBtn, roomTitle, spacer, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        contentArea.getChildren().add(toolbar);

        // ── Load schedules ────────────────────────────────────────────────────
        new Thread(() -> {
            try {
                List<Schedule> schedules = ScheduleRepository.getByRoomId(room.getId());

                schedules.sort((a, b) -> {
                    int dayCmp = a.getDay().compareTo(b.getDay());
                    if (dayCmp != 0) return dayCmp;
                    return a.getStartTime().compareTo(b.getStartTime());
                });

                Platform.runLater(() -> {
                    if (schedules.isEmpty()) {
                        Label empty = new Label("No schedules for this room yet.");
                        empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                        return;
                    }
                    for (Schedule s : schedules) {
                        contentArea.getChildren().add(buildScheduleCard(s, room));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load schedules.");
                    error.setStyle("-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(error);
                });
            }
        }).start();
    }

    private VBox buildScheduleCard(Schedule schedule, Room room) {
        VBox card = new VBox(6);

        String baseStyle =
                "-fx-background-color: white; -fx-border-color: #e0ded8; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16; -fx-cursor: hand;";
        String hoverStyle =
                "-fx-background-color: #fafaf8; -fx-border-color: #c8c6c0; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16; -fx-cursor: hand;";

        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));
        card.setOnMouseClicked(e -> openScheduleModal(schedule, room));

        Label dayLabel = new Label(schedule.getDay().toString());
        dayLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        Label timeLabel = new Label(schedule.getStartTime() + " – " + schedule.getEndTime());
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        card.getChildren().addAll(dayLabel, timeLabel);
        return card;
    }

    // ── Modal ─────────────────────────────────────────────────────────────────

    private void openScheduleModal(Schedule schedule, Room room) {
        boolean isNew = schedule == null;

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setResizable(false);

        ComboBox<Schedule.Day> dayBox = new ComboBox<>();
        dayBox.getItems().addAll(Schedule.Day.values());
        dayBox.setValue(isNew ? Schedule.Day.MONDAY : schedule.getDay());
        styleComboBox(dayBox);

        TextField startField = new TextField(isNew ? "" : schedule.getStartTime());
        startField.setPromptText("HH:mm  (e.g. 08:00)");
        styleTextField(startField);

        TextField endField = new TextField(isNew ? "" : schedule.getEndTime());
        endField.setPromptText("HH:mm  (e.g. 10:00)");
        styleTextField(endField);

        // Room is locked to context — shown as a read-only label
        Label roomLabel = new Label(room.getRoomName());
        roomLabel.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #2c2c2a; " +
                        "-fx-background-color: #f0ede6; -fx-background-radius: 6; " +
                        "-fx-padding: 8 10; -fx-border-color: #e0ded8; " +
                        "-fx-border-radius: 6; -fx-border-width: 0.5;");

        VBox form = new VBox(14);
        form.setPadding(new Insets(28));
        form.setPrefWidth(400);

        Label heading = new Label(isNew ? "New Schedule" : "Edit Schedule");
        heading.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");

        form.getChildren().addAll(
                heading,
                labeledField("Room", roomLabel),
                labeledField("Day", dayBox),
                labeledField("Start Time", startField),
                labeledField("End Time", endField)
        );

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

        actions.getChildren().addAll(cancelBtn, saveBtn);

        if (!isNew) {
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #a32d2d; " +
                            "-fx-font-size: 13px; -fx-border-color: #e8c8c8; -fx-border-radius: 6; " +
                            "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                            "-fx-padding: 7 16; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                if (confirmDialog(modal, "Delete Schedule",
                        "Delete this schedule? Any sections using it may be affected.")) {
                    new Thread(() -> {
                        try {
                            ScheduleService.deleteSchedule(schedule.getId());
                            Platform.runLater(() -> {
                                modal.close();
                                loadRoomSchedules(room);
                            });
                        } catch (IllegalStateException ex) {
                            Platform.runLater(() -> showError(modal, ex.getMessage()));
                        } catch (Exception ex) {
                            Platform.runLater(() ->
                                    showError(modal, "Failed to delete schedule."));
                        }
                    }).start();
                }
            });

            Region btnSpacer = new Region();
            HBox.setHgrow(btnSpacer, Priority.ALWAYS);
            actions.getChildren().addAll(0, List.of(deleteBtn, btnSpacer));
        }

        saveBtn.setOnAction(e -> {
            String start = startField.getText().trim();
            String end   = endField.getText().trim();

            new Thread(() -> {
                try {
                    if (isNew) {
                        ScheduleService.createSchedule(
                                dayBox.getValue(), start, end, room.getId());
                    } else {
                        ScheduleService.updateSchedule(
                                schedule, dayBox.getValue(), start, end, room.getId());
                    }
                    Platform.runLater(() -> {
                        modal.close();
                        loadRoomSchedules(room);
                    });
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    Platform.runLater(() -> showError(modal, ex.getMessage()));
                } catch (Exception ex) {
                    Platform.runLater(() -> showError(modal, "Failed to save schedule."));
                }
            }).start();
        });

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
    }

    private boolean confirmDialog(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait()
                .filter(b -> b == ButtonType.OK)
                .isPresent();
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