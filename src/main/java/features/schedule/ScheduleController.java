package features.schedule;

import features.rooms.RoomRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Room;
import models.Schedule;
import models.Faculty;
import features.faculty.FacultyRepository;
import models.Section;
import features.section.SectionRepository;
import features.section.SectionService;
import models.Subject;
import features.subject.SubjectRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                    Label err = new Label("Failed to load rooms.");
                    err.setStyle("-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(err);
                });
            }
        }).start();
    }

    private VBox buildRoomCard(Room room) {
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
        card.setOnMouseClicked(e -> loadRoomSections(room));

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        Label meta = new Label(room.getRoomType() + " · Capacity: " + room.getCapacity());
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");
        metaRow.getChildren().add(meta);

        if (!room.isActive()) {
            Label badge = new Label("INACTIVE");
            badge.setStyle(
                    "-fx-background-color: #f0ede6; -fx-text-fill: #888780; " +
                            "-fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 2 6;");
            metaRow.getChildren().add(badge);
        }

        Label name = new Label(room.getRoomName());
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        card.getChildren().addAll(metaRow, name);
        return card;
    }

    // ── Level 2: Sections for a room ─────────────────────────────────────────

    private void loadRoomSections(Room room) {
        contentArea.getChildren().clear();

        // ── Toolbar ───────────────────────────────────────────────────────────
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

        Button addBtn = new Button("+ New Section");
        addBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        addBtn.setOnAction(e -> openSectionModal(null, room));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox toolbar = new HBox(10, backBtn, roomTitle, spacer, addBtn);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        contentArea.getChildren().add(toolbar);

        // ── Load sections that have at least one schedule in this room ─────────
        new Thread(() -> {
            try {
                List<Schedule> roomSchedules = ScheduleRepository.getByRoomId(room.getId());
                List<String> roomScheduleIds = roomSchedules.stream()
                        .map(Schedule::getId)
                        .collect(Collectors.toList());

                List<Section> allSections = SectionRepository.getAll();
                List<Section> roomSections = allSections.stream()
                        .filter(s -> s.getScheduleIds().stream()
                                .anyMatch(roomScheduleIds::contains))
                        .collect(Collectors.toList());

                // build scheduleId → Schedule map for display
                Map<String, Schedule> scheduleMap = roomSchedules.stream()
                        .collect(Collectors.toMap(Schedule::getId, s -> s));

                // fetch faculty names
                List<Faculty> allFaculty = FacultyRepository.getAll();
                Map<String, String> facultyNames = allFaculty.stream()
                        .collect(Collectors.toMap(Faculty::getId, Faculty::getName));

                // fetch subject names
                List<Subject> allSubjects = SubjectRepository.getAll();
                Map<String, String> subjectNames = allSubjects.stream()
                        .collect(Collectors.toMap(
                                Subject::getSubjectCode, Subject::getSubjectName));

                Platform.runLater(() -> {
                    if (roomSections.isEmpty()) {
                        Label empty = new Label("No sections scheduled in this room yet.");
                        empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                        return;
                    }
                    for (Section s : roomSections) {
                        contentArea.getChildren().add(
                                buildSectionCard(s, room, scheduleMap,
                                        facultyNames, subjectNames));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label err = new Label("Failed to load sections.");
                    err.setStyle("-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(err);
                });
            }
        }).start();
    }

    private VBox buildSectionCard(Section section, Room room,
                                  Map<String, Schedule> scheduleMap,
                                  Map<String, String> facultyNames,
                                  Map<String, String> subjectNames) {
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
        card.setOnMouseClicked(e -> openSectionModal(section, room));

        // ── Header: section code + slots ──────────────────────────────────────
        String subjectName = subjectNames.getOrDefault(
                section.getSubjectCode(), section.getSubjectCode());
        String facultyName = facultyNames.getOrDefault(
                section.getFacultyId(), section.getFacultyId());

        Label nameLabel = new Label(section.getSectionCode() + "  ·  " + subjectName);
        nameLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        Label metaLabel = new Label(facultyName
                + "  ·  " + section.getAvailableSlots()
                + "/" + section.getCapacity() + " slots");
        metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

        card.getChildren().addAll(nameLabel, metaLabel);

        // ── Schedule rows (only slots in this room) ───────────────────────────
        for (String sid : section.getScheduleIds()) {
            Schedule s = scheduleMap.get(sid);
            if (s == null) continue; // slot belongs to a different room
            Label slotLabel = new Label(
                    "  " + s.getDay() + "  " + s.getStartTime() + " – " + s.getEndTime());
            slotLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #5f5e5a;");
            card.getChildren().add(slotLabel);
        }

        return card;
    }

    // ── Modal ─────────────────────────────────────────────────────────────────

    private void openSectionModal(Section section, Room contextRoom) {
        // fetch all needed data before showing modal
        new Thread(() -> {
            try {
                List<Room> allRooms = RoomRepository.getAll().stream()
                        .filter(Room::isActive)
                        .sorted((a, b) -> a.getRoomName()
                                .compareToIgnoreCase(b.getRoomName()))
                        .collect(Collectors.toList());

                List<Subject> subjects = SubjectRepository.getAll().stream()
                        .sorted((a, b) -> a.getSubjectCode()
                                .compareToIgnoreCase(b.getSubjectCode()))
                        .collect(Collectors.toList());

                List<Faculty> faculty = FacultyRepository.getAll().stream()
                        .sorted((a, b) -> a.getName()
                                .compareToIgnoreCase(b.getName()))
                        .collect(Collectors.toList());

                // if editing, fetch existing schedules for this section
                List<Schedule> existingSchedules = new ArrayList<>();
                if (section != null) {
                    for (String sid : section.getScheduleIds()) {
                        Schedule s = ScheduleRepository.getById(sid);
                        if (s != null) existingSchedules.add(s);
                    }
                }

                final List<Schedule> finalExisting = existingSchedules;
                Platform.runLater(() ->
                        showModal(section, contextRoom, allRooms,
                                subjects, faculty, finalExisting));

            } catch (Exception e) {
                Platform.runLater(() -> showError(null, "Failed to load form data."));
            }
        }).start();
    }

    private void showModal(Section section, Room contextRoom,
                           List<Room> allRooms, List<Subject> subjects,
                           List<Faculty> faculty, List<Schedule> existingSchedules) {

        boolean isNew = section == null;

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setResizable(false);

        // ── Subject dropdown ──────────────────────────────────────────────────
        ComboBox<Subject> subjectBox = new ComboBox<>();
        subjectBox.getItems().addAll(subjects);
        subjectBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Subject s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null
                        : s.getSubjectCode() + " — " + s.getSubjectName());
            }
        });
        subjectBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Subject s, boolean empty) {
                super.updateItem(s, empty);
                setText(empty || s == null ? null
                        : s.getSubjectCode() + " — " + s.getSubjectName());
            }
        });
        if (!isNew) {
            subjects.stream()
                    .filter(s -> s.getSubjectCode().equals(section.getSubjectCode()))
                    .findFirst().ifPresent(subjectBox::setValue);
        }
        styleComboBox(subjectBox);
        subjectBox.setDisable(!isNew); // subject locked on edit

        // ── Faculty dropdown ──────────────────────────────────────────────────
        ComboBox<Faculty> facultyBox = new ComboBox<>();
        facultyBox.getItems().addAll(faculty);
        facultyBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Faculty f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null
                        : f.getName() + " (" + f.getDepartment() + ")");
            }
        });
        facultyBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Faculty f, boolean empty) {
                super.updateItem(f, empty);
                setText(empty || f == null ? null
                        : f.getName() + " (" + f.getDepartment() + ")");
            }
        });
        if (!isNew) {
            faculty.stream()
                    .filter(f -> f.getId().equals(section.getFacultyId()))
                    .findFirst().ifPresent(facultyBox::setValue);
        }
        styleComboBox(facultyBox);

        // ── Capacity field ────────────────────────────────────────────────────
        TextField capacityField = new TextField(
                isNew ? "" : String.valueOf(section.getCapacity()));
        capacityField.setPromptText("e.g. 30");
        styleTextField(capacityField);

        // ── Dynamic schedule slots ────────────────────────────────────────────
        VBox slotsContainer = new VBox(10);

        if (isNew) {
            // start with one slot pre-selected to contextRoom
            slotsContainer.getChildren().add(
                    buildSlotRow(slotsContainer, allRooms, null, contextRoom));
        } else {
            for (Schedule s : existingSchedules) {
                // find room for this schedule
                Room slotRoom = allRooms.stream()
                        .filter(r -> r.getId().equals(s.getRoomId()))
                        .findFirst().orElse(contextRoom);
                slotsContainer.getChildren().add(
                        buildSlotRow(slotsContainer, allRooms, s, slotRoom));
            }
        }

        Button addSlotBtn = new Button("+ Add Schedule Slot");
        addSlotBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #2c2c2a; " +
                        "-fx-font-size: 12px; -fx-border-color: #e0ded8; -fx-border-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                        "-fx-padding: 6 12; -fx-cursor: hand;");
        addSlotBtn.setOnAction(e ->
                slotsContainer.getChildren().add(
                        buildSlotRow(slotsContainer, allRooms, null, contextRoom)));

        // ── Form layout ───────────────────────────────────────────────────────
        VBox form = new VBox(14);
        form.setPadding(new Insets(28));
        form.setPrefWidth(500);

        Label heading = new Label(isNew ? "New Section" : "Edit Section");
        heading.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");

        Label slotsLabel = new Label("Schedules");
        slotsLabel.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #888780; -fx-font-weight: 500;");

        form.getChildren().addAll(
                heading,
                labeledField("Subject", subjectBox),
                labeledField("Faculty", facultyBox),
                labeledField("Capacity", capacityField),
                slotsLabel,
                slotsContainer,
                addSlotBtn
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

        Button saveBtn = new Button(isNew ? "Create Section" : "Save Changes");
        saveBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-background-radius: 6; " +
                        "-fx-padding: 7 16; -fx-cursor: hand;");

        actions.getChildren().addAll(cancelBtn, saveBtn);

        // ── Edit-only: Delete ─────────────────────────────────────────────────
        if (!isNew) {
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #a32d2d; " +
                            "-fx-font-size: 13px; -fx-border-color: #e8c8c8; -fx-border-radius: 6; " +
                            "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                            "-fx-padding: 7 16; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                if (confirmDialog(modal, "Delete Section",
                        "Delete section \"" + section.getSectionCode()
                                + "\"? This will also delete all its schedules.")) {
                    new Thread(() -> {
                        try {
                            SectionService.deleteSection(section);
                            Platform.runLater(() -> {
                                modal.close();
                                loadRoomSections(contextRoom);
                            });
                        } catch (IllegalStateException ex) {
                            Platform.runLater(() -> showError(modal, ex.getMessage()));
                        } catch (Exception ex) {
                            Platform.runLater(() ->
                                    showError(modal, "Failed to delete section."));
                        }
                    }).start();
                }
            });

            Region btnSpacer = new Region();
            HBox.setHgrow(btnSpacer, Priority.ALWAYS);
            actions.getChildren().addAll(0, List.of(deleteBtn, btnSpacer));
        }

        // ── Save handler ──────────────────────────────────────────────────────
        saveBtn.setOnAction(e -> {
            Subject selectedSubject = subjectBox.getValue();
            Faculty selectedFaculty = facultyBox.getValue();
            String capacityText = capacityField.getText().trim();

            if (selectedSubject == null) {
                showError(modal, "Please select a subject.");
                return;
            }
            if (selectedFaculty == null) {
                showError(modal, "Please select a faculty member.");
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

            // collect slots from UI
            List<SectionService.ScheduleSlot> slots = new ArrayList<>();
            for (Node node : slotsContainer.getChildren()) {
                if (!(node instanceof VBox slotBox)) continue;
                try {
                    SectionService.ScheduleSlot slot = extractSlot(slotBox);
                    if (slot == null) {
                        showError(modal, "Please fill in all schedule slot fields.");
                        return;
                    }
                    slots.add(slot);
                } catch (Exception ex) {
                    showError(modal, "Invalid slot data: " + ex.getMessage());
                    return;
                }
            }

            if (slots.isEmpty()) {
                showError(modal, "At least one schedule slot is required.");
                return;
            }

            new Thread(() -> {
                try {
                    if (isNew) {
                        SectionService.createSection(
                                selectedSubject.getSubjectCode(),
                                selectedFaculty.getId(),
                                capacity,
                                slots);
                    } else {
                        SectionService.updateSection(
                                section,
                                selectedFaculty.getId(),
                                capacity,
                                slots);
                    }
                    Platform.runLater(() -> {
                        modal.close();
                        loadRoomSections(contextRoom);
                    });
                } catch (IllegalArgumentException | IllegalStateException ex) {
                    Platform.runLater(() -> showError(modal, ex.getMessage()));
                } catch (Exception ex) {
                    Platform.runLater(() ->
                            showError(modal, "Failed to save section."));
                }
            }).start();
        });

        form.getChildren().add(actions);

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");

        modal.setScene(new Scene(scroll, 520, 640));
        modal.showAndWait();
    }

    // ── Slot row builder ──────────────────────────────────────────────────────

    private VBox buildSlotRow(VBox container, List<Room> allRooms,
                              Schedule existing, Room defaultRoom) {
        VBox slotBox = new VBox(10);
        slotBox.setStyle(
                "-fx-background-color: #fafaf8; -fx-border-color: #e0ded8; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; -fx-padding: 12;");

        // Room dropdown
        ComboBox<Room> roomBox = new ComboBox<>();
        roomBox.getItems().addAll(allRooms);
        roomBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null
                        : r.getRoomName() + " (cap: " + r.getCapacity() + ")");
            }
        });
        roomBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Room r, boolean empty) {
                super.updateItem(r, empty);
                setText(empty || r == null ? null
                        : r.getRoomName() + " (cap: " + r.getCapacity() + ")");
            }
        });
        if (existing != null) {
            allRooms.stream()
                    .filter(r -> r.getId().equals(existing.getRoomId()))
                    .findFirst().ifPresent(roomBox::setValue);
        } else if (defaultRoom != null) {
            allRooms.stream()
                    .filter(r -> r.getId().equals(defaultRoom.getId()))
                    .findFirst().ifPresent(roomBox::setValue);
        }
        styleComboBox(roomBox);

        // Day dropdown
        ComboBox<Schedule.Day> dayBox = new ComboBox<>();
        dayBox.getItems().addAll(Schedule.Day.values());
        dayBox.setValue(existing != null ? existing.getDay() : Schedule.Day.MONDAY);
        styleComboBox(dayBox);

        // Time fields
        TextField startField = new TextField(existing != null ? existing.getStartTime() : "");
        startField.setPromptText("HH:mm");
        styleTextField(startField);

        TextField endField = new TextField(existing != null ? existing.getEndTime() : "");
        endField.setPromptText("HH:mm");
        styleTextField(endField);

        // Remove button
        Button removeBtn = new Button("✕ Remove");
        removeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #a32d2d; " +
                        "-fx-font-size: 11px; -fx-cursor: hand; -fx-padding: 2 0;");
        removeBtn.setOnAction(e -> {
            if (container.getChildren().size() > 1) {
                container.getChildren().remove(slotBox);
            }
        });

        HBox timeRow = new HBox(10,
                labeledFieldNode("Start", startField),
                labeledFieldNode("End", endField));
        timeRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(labeledFieldNode("Start", startField), Priority.ALWAYS);
        HBox.setHgrow(labeledFieldNode("End", endField), Priority.ALWAYS);

        HBox removeRow = new HBox(removeBtn);
        removeRow.setAlignment(Pos.CENTER_RIGHT);

        slotBox.getChildren().addAll(
                labeledFieldNode("Room", roomBox),
                labeledFieldNode("Day", dayBox),
                new HBox(10,
                        labeledFieldNode("Start Time", startField),
                        labeledFieldNode("End Time", endField)),
                removeRow
        );

        // store references as user data for extraction
        slotBox.setUserData(new Object[]{roomBox, dayBox, startField, endField});

        return slotBox;
    }

    @SuppressWarnings("unchecked")
    private SectionService.ScheduleSlot extractSlot(VBox slotBox) {
        Object[] data = (Object[]) slotBox.getUserData();
        if (data == null) return null;

        ComboBox<Room> roomBox       = (ComboBox<Room>) data[0];
        ComboBox<Schedule.Day> dayBox = (ComboBox<Schedule.Day>) data[1];
        TextField startField          = (TextField) data[2];
        TextField endField            = (TextField) data[3];

        Room room = roomBox.getValue();
        Schedule.Day day = dayBox.getValue();
        String start = startField.getText().trim();
        String end   = endField.getText().trim();

        if (room == null || day == null || start.isEmpty() || end.isEmpty()) return null;

        return new SectionService.ScheduleSlot(day, start, end, room.getId());
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private VBox labeledField(String text, Node field) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #888780; -fx-font-weight: 500;");
        if (field instanceof ComboBox<?> cb) cb.setMaxWidth(Double.MAX_VALUE);
        return new VBox(5, label, field);
    }

    private VBox labeledFieldNode(String text, Node field) {
        Label label = new Label(text);
        label.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: #888780; -fx-font-weight: 500;");
        if (field instanceof ComboBox<?> cb) cb.setMaxWidth(Double.MAX_VALUE);
        VBox box = new VBox(4, label, field);
        VBox.setVgrow(field, Priority.ALWAYS);
        return box;
    }

    private void styleTextField(TextField f) {
        f.setStyle(
                "-fx-background-color: white; -fx-border-color: #e0ded8; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-padding: 8 10; -fx-font-size: 13px;");
    }

    private <T> void styleComboBox(ComboBox<T> cb) {
        cb.setStyle(
                "-fx-background-color: white; -fx-border-color: #e0ded8; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-font-size: 13px;");
        cb.setMaxWidth(Double.MAX_VALUE);
    }

    private boolean confirmDialog(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait().filter(b -> b == ButtonType.OK).isPresent();
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