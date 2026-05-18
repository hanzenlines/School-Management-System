package features.enrollment;

import features.rooms.RoomRepository;
import features.schedule.ScheduleRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Room;
import models.Schedule;
import models.section.Section;
import models.student.Student;
import models.subject.Subject;

import java.util.List;

public class SectionPickerController {

    @FXML private Label subjectTitleLabel;
    @FXML private Label subjectCodeLabel;
    @FXML private VBox sectionList;

    private Student student;
    private Subject subject;
    private Runnable onSuccess;

    public void initData(Student student, Subject subject, Runnable onSuccess) {
        this.student = student;
        this.subject = subject;
        this.onSuccess = onSuccess;

        subjectTitleLabel.setText(subject.getSubjectName());
        subjectCodeLabel.setText(subject.getSubjectCode());

        loadSections();
    }

    private void loadSections() {
        sectionList.getChildren().setAll(buildLabel("Loading sections..."));

        Thread thread = new Thread(() -> {
            try {
                List<Section> sections =
                        EnrollmentService.getAvailableSections(subject.getSubjectCode());

                Platform.runLater(() -> renderSections(sections));

            } catch (Exception e) {
                Platform.runLater(() ->
                        sectionList.getChildren()
                                .setAll(buildLabel("Failed to load sections: " + e.getMessage())));
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    private void renderSections(List<Section> sections) {
        sectionList.getChildren().clear();

        if (sections.isEmpty()) {
            sectionList.getChildren().add(buildLabel("No sections available."));
            return;
        }

        for (Section section : sections) {
            sectionList.getChildren().add(buildSectionCard(section));
        }
    }

    private VBox buildSectionCard(Section section) {
        VBox card = new VBox(4);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8;" +
                        "-fx-border-color: #dddcda; -fx-border-radius: 8; -fx-cursor: hand;");
        card.setPadding(new Insets(12, 16, 12, 16));

        Label sectionLabel = new Label("Section " + section.getId());
        sectionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c2c2a;");

        Label scheduleLabel = new Label(resolveSchedules(section.getScheduleIds()));
        scheduleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        Label slotsLabel = new Label(
                section.getCurrentCount() + "/" + section.getCapacity() + " slots");
        slotsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #555452;");

        card.getChildren().addAll(sectionLabel, scheduleLabel, slotsLabel);
        card.setOnMouseClicked(e -> enroll(section));
        return card;
    }

    private void enroll(Section section) {
        Thread thread = new Thread(() -> {
            try {
                // addToCart does all validation; throws on any rule violation
                EnrollmentService.addToCart(student, section);

                Platform.runLater(() -> {
                    if (onSuccess != null) onSuccess.run();
                    close();
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        sectionList.getChildren()
                                .add(buildLabel("⚠ " + e.getMessage())));
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void handleCancel() {
        close();
    }

    private void close() {
        Stage stage = (Stage) sectionList.getScene().getWindow();
        stage.close();
    }

    private Label buildLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");
        return label;
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