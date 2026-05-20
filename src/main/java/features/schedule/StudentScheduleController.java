package features.schedule;

import features.enrollment.EnrollmentRepository;
import features.rooms.RoomRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import models.Enrollment;
import models.Room;
import models.Schedule;
import models.enums.Status;
import models.Section;
import features.section.SectionRepository;
import models.Student;
import models.Subject;
import features.subject.SubjectRepository;

import java.util.*;
import java.util.stream.Collectors;

public class StudentScheduleController {

    @FXML private VBox contentArea;

    private Student student;

    // Day display order
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

    public void initData(Student student) {
        this.student = student;
        loadSchedule();
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private void loadSchedule() {
        contentArea.getChildren().clear();

        new Thread(() -> {
            try {
                // get all enrolled/pending enrollments for this student
                List<Enrollment> enrollments = EnrollmentRepository
                        .getByStudentId(student.getId())
                        .stream()
                        .filter(e -> e.getStatus() == Status.ENROLLED
                                || e.getStatus() == Status.PENDING)
                        .collect(Collectors.toList());

                if (enrollments.isEmpty()) {
                    Platform.runLater(() -> {
                        Label empty = new Label("No enrolled subjects yet.");
                        empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                    });
                    return;
                }

                // fetch all sections, schedules, rooms, subjects needed
                Map<Schedule.Day, List<ScheduleEntry>> byDay = new LinkedHashMap<>();
                for (Schedule.Day day : DAY_ORDER) byDay.put(day, new ArrayList<>());

                for (Enrollment enrollment : enrollments) {
                    Section section = SectionRepository.getById(enrollment.getSectionId());
                    if (section == null) continue;

                    Subject subject = SubjectRepository.getByCode(section.getSubjectCode());
                    String subjectName = subject != null
                            ? subject.getSubjectName() : section.getSubjectCode();

                    for (String scheduleId : section.getScheduleIds()) {
                        Schedule schedule = ScheduleRepository.getById(scheduleId);
                        if (schedule == null) continue;

                        Room room = RoomRepository.getById(schedule.getRoomId());
                        String roomName = room != null ? room.getRoomName() : "Unknown Room";

                        ScheduleEntry entry = new ScheduleEntry(
                                section.getSectionCode(),
                                section.getSubjectCode(),
                                subjectName,
                                schedule.getStartTime(),
                                schedule.getEndTime(),
                                roomName,
                                enrollment.getStatus()
                        );

                        byDay.get(schedule.getDay()).add(entry);
                    }
                }

                // sort each day's entries by start time
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

            // ── Entry cards for this day ───────────────────────────────────────
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

        // ── Time + room row ───────────────────────────────────────────────────
        Label timeLabel = new Label(
                entry.startTime() + " – " + entry.endTime()
                        + "  ·  " + entry.roomName());
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

        // ── Subject name ──────────────────────────────────────────────────────
        Label subjectLabel = new Label(entry.subjectName());
        subjectLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        // ── Section code + subject code ───────────────────────────────────────
        Label metaLabel = new Label(
                entry.sectionCode() + "  ·  " + entry.subjectCode());
        metaLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        card.getChildren().addAll(timeLabel, subjectLabel, metaLabel);

        // ── Pending badge if not yet confirmed ────────────────────────────────
        if (entry.status() == Status.PENDING) {
            Label badge = new Label("PENDING");
            badge.setStyle(
                    "-fx-background-color: #fdf0e0; -fx-text-fill: #c07000; " +
                            "-fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 2 6;");
            card.getChildren().add(badge);
        }

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
            Status status
    ) {}
}