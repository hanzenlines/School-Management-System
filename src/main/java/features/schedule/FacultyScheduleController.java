package features.schedule;

import features.rooms.RoomRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import models.Room;
import models.Schedule;
import models.faculty.Faculty;
import models.section.Section;
import models.section.SectionRepository;
import models.subject.Subject;
import models.subject.SubjectRepository;

import java.util.*;

public class FacultyScheduleController {

    @FXML private VBox contentArea;

    private Faculty faculty;

    private static final List<Schedule.Day> DAY_ORDER = List.of(
            Schedule.Day.MONDAY,
            Schedule.Day.TUESDAY,
            Schedule.Day.WEDNESDAY,
            Schedule.Day.THURSDAY,
            Schedule.Day.FRIDAY,
            Schedule.Day.SATURDAY,
            Schedule.Day.SUNDAY
    );

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData(Faculty faculty) {
        this.faculty = faculty;
        loadSchedule();
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private void loadSchedule() {
        contentArea.getChildren().clear();

        new Thread(() -> {
            try {
                List<Section> sections =
                        SectionRepository.getByFacultyId(faculty.getId());

                if (sections.isEmpty()) {
                    Platform.runLater(() -> {
                        Label empty = new Label("No sections assigned yet.");
                        empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                    });
                    return;
                }

                // build day → entries map
                Map<Schedule.Day, List<ScheduleEntry>> byDay = new LinkedHashMap<>();
                for (Schedule.Day day : DAY_ORDER) byDay.put(day, new ArrayList<>());

                for (Section section : sections) {
                    Subject subject = SubjectRepository.getByCode(section.getSubjectCode());
                    String subjectName = subject != null
                            ? subject.getSubjectName() : section.getSubjectCode();

                    for (String scheduleId : section.getScheduleIds()) {
                        Schedule schedule = ScheduleRepository.getById(scheduleId);
                        if (schedule == null) continue;

                        Room room = RoomRepository.getById(schedule.getRoomId());
                        String roomName = room != null ? room.getRoomName() : "Unknown Room";

                        byDay.get(schedule.getDay()).add(new ScheduleEntry(
                                section.getSectionCode(),
                                section.getSubjectCode(),
                                subjectName,
                                schedule.getStartTime(),
                                schedule.getEndTime(),
                                roomName,
                                section.getCurrentCount(),
                                section.getCapacity()
                        ));
                    }
                }

                // sort each day by start time
                byDay.values().forEach(list ->
                        list.sort(Comparator.comparing(ScheduleEntry::startTime)));

                Platform.runLater(() -> renderSchedule(byDay));

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load schedule.");
                    error.setStyle("-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(error);
                });
            }
        }).start();
    }

    // ── Render ────────────────────────────────────────────────────────────────

    private void renderSchedule(Map<Schedule.Day, List<ScheduleEntry>> byDay) {
        boolean hasAny = byDay.values().stream().anyMatch(list -> !list.isEmpty());

        if (!hasAny) {
            Label empty = new Label("No schedule entries found.");
            empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
            contentArea.getChildren().add(empty);
            return;
        }

        for (Schedule.Day day : DAY_ORDER) {
            List<ScheduleEntry> entries = byDay.get(day);
            if (entries.isEmpty()) continue;

            // ── Day header ────────────────────────────────────────────────────
            Label dayHeader = new Label(day.toString());
            dayHeader.setStyle(
                    "-fx-font-size: 12px; -fx-font-weight: 600; " +
                            "-fx-text-fill: #888780; -fx-padding: 8 0 4 0;");
            contentArea.getChildren().add(dayHeader);

            for (ScheduleEntry entry : entries) {
                contentArea.getChildren().add(buildEntryCard(entry));
            }
        }
    }

    private VBox buildEntryCard(ScheduleEntry entry) {
        VBox card = new VBox(5);
        card.setStyle(
                "-fx-background-color: white; -fx-border-color: #e0ded8; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 14;");

        // ── Time + room ───────────────────────────────────────────────────────
        Label timeLabel = new Label(
                entry.startTime() + " – " + entry.endTime()
                        + "  ·  " + entry.roomName());
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

        // ── Subject name ──────────────────────────────────────────────────────
        Label subjectLabel = new Label(entry.subjectName());
        subjectLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        // ── Section code + enrollment count ───────────────────────────────────
        Label metaLabel = new Label(
                entry.sectionCode()
                        + "  ·  " + entry.subjectCode()
                        + "  ·  " + entry.currentCount() + "/" + entry.capacity() + " students");
        metaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        card.getChildren().addAll(timeLabel, subjectLabel, metaLabel);
        return card;
    }

    // ── Schedule entry record ─────────────────────────────────────────────────

    private record ScheduleEntry(
            String sectionCode,
            String subjectCode,
            String subjectName,
            String startTime,
            String endTime,
            String roomName,
            int currentCount,
            int capacity
    ) {}
}