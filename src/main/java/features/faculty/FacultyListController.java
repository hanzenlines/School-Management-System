package features.faculty;

import features.schedule.ScheduleRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.*;
import features.section.SectionRepository;
import features.rooms.RoomRepository;
import features.subject.SubjectRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FacultyListController {

    @FXML private VBox contentArea;

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData() {
        loadFaculty();
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private void loadFaculty() {
        contentArea.getChildren().clear();

        new Thread(() -> {
            try {
                List<Faculty> faculty = FacultyRepository.getAll();

                Platform.runLater(() -> {
                    if (faculty.isEmpty()) {
                        Label empty = new Label("No faculty found.");
                        empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                        return;
                    }
                    for (Faculty f : faculty) {
                        contentArea.getChildren().add(buildCard(f));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load faculty.");
                    error.setStyle("-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(error);
                });
            }
        }).start();
    }

    // ── Card ──────────────────────────────────────────────────────────────────

    private VBox buildCard(Faculty faculty) {
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
        card.setOnMouseClicked(e -> openFacultyModal(faculty));

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label(faculty.getEmployeeNumber()
                + "  ·  " + faculty.getDepartment()
                + "  ·  " + faculty.getPosition());
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");
        metaRow.getChildren().add(meta);

        Label nameLabel = new Label(faculty.getName());
        nameLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        Label emailLabel = new Label(faculty.getEmail());
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        card.getChildren().addAll(metaRow, nameLabel, emailLabel);
        return card;
    }

    // ── Modal ─────────────────────────────────────────────────────────────────

    private void openFacultyModal(Faculty faculty) {
        // fetch assigned sections before showing modal
        new Thread(() -> {
            try {
                List<Section> sections = SectionRepository.getByFacultyId(faculty.getId());
                List<Subject> allSubjects = SubjectRepository.getAll();
                Map<String, String> subjectNames = allSubjects.stream()
                        .collect(Collectors.toMap(
                                Subject::getSubjectCode, Subject::getSubjectName,
                                (a, b) -> a));

                // fetch schedules for display
                Map<String, Schedule> scheduleMap = new java.util.HashMap<>();
                for (Section s : sections) {
                    for (String sid : s.getScheduleIds()) {
                        if (!scheduleMap.containsKey(sid)) {
                            Schedule sch = ScheduleRepository.getById(sid);
                            if (sch != null) scheduleMap.put(sid, sch);
                        }
                    }
                }

                // fetch room names
                List<Room> allRooms = RoomRepository.getAll();
                Map<String, String> roomNames = allRooms.stream()
                        .collect(Collectors.toMap(Room::getId, Room::getRoomName,
                                (a, b) -> a));

                Platform.runLater(() ->
                        showModal(faculty, sections, subjectNames,
                                scheduleMap, roomNames));

            } catch (Exception e) {
                Platform.runLater(() -> showError(null, "Failed to load faculty data."));
            }
        }).start();
    }

    private void showModal(Faculty faculty, List<Section> sections,
                           Map<String, String> subjectNames,
                           Map<String, Schedule> scheduleMap,
                           Map<String, String> roomNames) {

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setResizable(false);

        // ── Editable fields ───────────────────────────────────────────────────
        TextField departmentField = new TextField(faculty.getDepartment());
        styleTextField(departmentField);

        TextField positionField = new TextField(faculty.getPosition());
        styleTextField(positionField);

        // ── Read-only fields ──────────────────────────────────────────────────
        Label employeeNumberLabel = new Label(faculty.getEmployeeNumber());
        styleReadOnly(employeeNumberLabel);

        Label emailLabel = new Label(faculty.getEmail());
        styleReadOnly(emailLabel);

        // ── Assigned sections list ────────────────────────────────────────────
        VBox sectionsBox = new VBox(8);

        if (sections.isEmpty()) {
            Label none = new Label("No sections assigned.");
            none.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");
            sectionsBox.getChildren().add(none);
        } else {
            for (Section s : sections) {
                sectionsBox.getChildren().add(
                        buildSectionRow(s, subjectNames, scheduleMap, roomNames));
            }
        }

        // ── Form layout ───────────────────────────────────────────────────────
        VBox form = new VBox(14);
        form.setPadding(new Insets(28));
        form.setPrefWidth(480);

        Label heading = new Label(faculty.getName());
        heading.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");

        Label sectionsHeading = new Label("Assigned Sections");
        sectionsHeading.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #888780; -fx-font-weight: 500;");

        form.getChildren().addAll(
                heading,
                labeledField("Employee Number", employeeNumberLabel),
                labeledField("Email", emailLabel),
                labeledField("Department", departmentField),
                labeledField("Position", positionField),
                sectionsHeading,
                sectionsBox
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

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-background-radius: 6; " +
                        "-fx-padding: 7 16; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            String dept = departmentField.getText().trim();
            String pos  = positionField.getText().trim();

            if (dept.isEmpty() || pos.isEmpty()) {
                showError(modal, "Department and position cannot be empty.");
                return;
            }

            new Thread(() -> {
                try {
                    faculty.setDepartment(dept);
                    faculty.setPosition(pos);
                    FacultyRepository.update(faculty);
                    Platform.runLater(() -> {
                        modal.close();
                        loadFaculty();
                    });
                } catch (IllegalArgumentException ex) {
                    Platform.runLater(() -> showError(modal, ex.getMessage()));
                } catch (Exception ex) {
                    Platform.runLater(() -> showError(modal, "Failed to save changes."));
                }
            }).start();
        });

        actions.getChildren().addAll(cancelBtn, saveBtn);
        form.getChildren().add(actions);

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");

        modal.setScene(new Scene(scroll, 500, 600));
        modal.showAndWait();
    }

    // ── Section row inside faculty modal ──────────────────────────────────────

    private VBox buildSectionRow(Section section,
                                 Map<String, String> subjectNames,
                                 Map<String, Schedule> scheduleMap,
                                 Map<String, String> roomNames) {
        VBox row = new VBox(4);
        row.setStyle(
                "-fx-background-color: #fafaf8; -fx-border-color: #e0ded8; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 6; -fx-padding: 10;");

        String subjectName = subjectNames.getOrDefault(
                section.getSubjectCode(), section.getSubjectCode());

        Label nameLabel = new Label(section.getSectionCode() + "  ·  " + subjectName);
        nameLabel.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        Label slotsLabel = new Label(
                section.getAvailableSlots() + "/" + section.getCapacity() + " slots available");
        slotsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

        row.getChildren().addAll(nameLabel, slotsLabel);

        // schedule slots
        for (String sid : section.getScheduleIds()) {
            Schedule s = scheduleMap.get(sid);
            if (s == null) continue;
            String roomName = roomNames.getOrDefault(s.getRoomId(), "Unknown Room");
            Label slotLabel = new Label(
                    "  " + s.getDay() + "  "
                            + s.getStartTime() + " – " + s.getEndTime()
                            + "  @  " + roomName);
            slotLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #5f5e5a;");
            row.getChildren().add(slotLabel);
        }

        return row;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private VBox labeledField(String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #888780; -fx-font-weight: 500;");
        return new VBox(5, label, field);
    }

    private void styleTextField(TextField f) {
        f.setStyle(
                "-fx-background-color: #fafaf8; -fx-border-color: #e0ded8; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-padding: 8 10; -fx-font-size: 13px;");
    }

    private void styleReadOnly(Label l) {
        l.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #2c2c2a; " +
                        "-fx-background-color: #f0ede6; -fx-background-radius: 6; " +
                        "-fx-padding: 8 10; -fx-border-color: #e0ded8; " +
                        "-fx-border-radius: 6; -fx-border-width: 0.5;");
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