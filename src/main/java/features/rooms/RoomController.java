package features.rooms;

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
import models.enums.RoomType;

import java.util.List;

public class RoomController {

    @FXML private VBox contentArea;

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData() {
        loadRooms();
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private void loadRooms() {
        contentArea.getChildren().clear();

        Button addBtn = new Button("+ New Room");
        addBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        addBtn.setOnAction(e -> openRoomModal(null));

        HBox toolbar = new HBox(addBtn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        contentArea.getChildren().add(toolbar);

        new Thread(() -> {
            try {
                List<Room> rooms = RoomService.getAllRooms();

                Platform.runLater(() -> {
                    if (rooms.isEmpty()) {
                        Label empty = new Label("No rooms found.");
                        empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                        return;
                    }
                    for (Room r : rooms) {
                        contentArea.getChildren().add(buildCard(r));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load rooms.");
                    error.setStyle("-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(error);
                });
                e.printStackTrace();
            }
        }).start();
    }

    // ── Card ──────────────────────────────────────────────────────────────────

    private VBox buildCard(Room room) {
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
        card.setOnMouseClicked(e -> openRoomModal(room));

        // ── Meta row: type · capacity · status badge ──────────────────────────
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label(
                room.getRoomType().toString() + " · Capacity: " + room.getCapacity());
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

        metaRow.getChildren().add(meta);

        if (!room.isActive()) {
            Label badge = new Label("INACTIVE");
            badge.setStyle(
                    "-fx-background-color: #f0ede6; -fx-text-fill: #888780; " +
                            "-fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 2 6;");
            metaRow.getChildren().add(badge);
        }

        // ── Room name ─────────────────────────────────────────────────────────
        Label nameLabel = new Label(room.getRoomName());
        nameLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        card.getChildren().addAll(metaRow, nameLabel);
        return card;
    }

    // ── Modal ─────────────────────────────────────────────────────────────────

    private void openRoomModal(Room room) {
        boolean isNew = room == null;

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setResizable(false);

        // ── Fields ────────────────────────────────────────────────────────────
        TextField nameField = new TextField(isNew ? "" : room.getRoomName());
        nameField.setPromptText("Room name (e.g. Room 101)");
        styleTextField(nameField);

        TextField capacityField = new TextField(isNew ? "" : String.valueOf(room.getCapacity()));
        capacityField.setPromptText("Capacity (e.g. 40)");
        styleTextField(capacityField);

        ComboBox<RoomType> typeBox = new ComboBox<>();
        typeBox.getItems().addAll(RoomType.values());
        typeBox.setValue(isNew ? RoomType.values()[0] : room.getRoomType());
        styleComboBox(typeBox);

        CheckBox activeCheck = new CheckBox("Active");
        activeCheck.setSelected(isNew || room.isActive());
        activeCheck.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c2c2a;");

        // ── Form layout ───────────────────────────────────────────────────────
        VBox form = new VBox(14);
        form.setPadding(new Insets(28));
        form.setPrefWidth(420);

        Label heading = new Label(isNew ? "New Room" : "Edit Room");
        heading.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");

        form.getChildren().addAll(
                heading,
                labeledField("Room Name", nameField),
                labeledField("Capacity", capacityField),
                labeledField("Room Type", typeBox),
                activeCheck
        );

        // ── Action buttons ────────────────────────────────────────────────────
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #888780; " +
                        "-fx-font-size: 13px; -fx-border-color: #e0ded8; -fx-border-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                        "-fx-padding: 7 16; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> modal.close());

        Button saveBtn = new Button(isNew ? "Create Room" : "Save Changes");
        saveBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-background-radius: 6; " +
                        "-fx-padding: 7 16; -fx-cursor: hand;");

        actions.getChildren().addAll(cancelBtn, saveBtn);

        // ── Edit-only: Delete button ───────────────────────────────────────────
        if (!isNew) {
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #a32d2d; " +
                            "-fx-font-size: 13px; -fx-border-color: #e8c8c8; -fx-border-radius: 6; " +
                            "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                            "-fx-padding: 7 16; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                if (confirmDialog(modal, "Delete Room",
                        "Delete \"" + room.getRoomName() + "\"? This cannot be undone.")) {
                    new Thread(() -> {
                        try {
                            RoomService.deleteRoom(room.getId());
                            Platform.runLater(() -> {
                                modal.close();
                                loadRooms();
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() ->
                                    showError(modal, "Failed to delete room."));
                        }
                    }).start();
                }
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            actions.getChildren().addAll(0, List.of(deleteBtn, spacer));
        }

        // ── Save handler ──────────────────────────────────────────────────────
        saveBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String capacityText = capacityField.getText().trim();

            if (name.isEmpty()) {
                showError(modal, "Room name cannot be empty.");
                return;
            }

            int capacity;
            try {
                capacity = Integer.parseInt(capacityText);
                if (capacity <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                showError(modal, "Capacity must be a positive number.");
                return;
            }

            RoomType type = typeBox.getValue();
            boolean active = activeCheck.isSelected();

            new Thread(() -> {
                try {
                    if (isNew) {
                        RoomService.createRoom(name, capacity, type);
                    } else {
                        RoomService.updateRoom(room, name, capacity, type, active);
                    }
                    Platform.runLater(() -> {
                        modal.close();
                        loadRooms();
                    });
                } catch (IllegalArgumentException ex) {
                    Platform.runLater(() -> showError(modal, ex.getMessage()));
                } catch (Exception ex) {
                    Platform.runLater(() -> showError(modal, "Failed to save room."));
                }
            }).start();
        });

        form.getChildren().add(actions);
        modal.setScene(new Scene(form, Color.WHITE));
        modal.showAndWait();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private VBox labeledField(String labelText, Control field) {
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
        alert.initOwner(owner);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}