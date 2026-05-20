package features.section;

import features.schedule.ScheduleRepository;
import models.Schedule;
import models.Course;
import features.course.CourseRepository;
import models.Section;
import models.Subject;
import features.subject.SubjectRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SectionService {

    private SectionService() {}

    // ── Create ────────────────────────────────────────────────────────────────

    /**
     * Creates schedules and a section in one atomic operation.
     * Checks room conflicts and faculty conflicts before saving anything.
     *
     * @param subjectCode   subject code
     * @param facultyId     faculty id
     * @param capacity      section capacity (must be <= room capacity of each slot)
     * @param slots         list of schedule slot descriptors (day, startTime, endTime, roomId)
     */
    public static void createSection(String subjectCode, String facultyId,
                                     int capacity, List<ScheduleSlot> slots)
            throws IOException, InterruptedException {

        // ── Validate inputs ───────────────────────────────────────────────────
        if (subjectCode == null || subjectCode.isBlank())
            throw new IllegalArgumentException("Subject cannot be empty");
        if (facultyId == null || facultyId.isBlank())
            throw new IllegalArgumentException("Faculty cannot be empty");
        if (capacity <= 0)
            throw new IllegalArgumentException("Capacity must be greater than 0");
        if (slots == null || slots.isEmpty())
            throw new IllegalArgumentException("At least one schedule slot is required");

        // ── Build candidate Schedule objects ──────────────────────────────────
        List<Schedule> candidates = new ArrayList<>();
        for (ScheduleSlot slot : slots) {
            if (slot.roomId() == null || slot.roomId().isBlank())
                throw new IllegalArgumentException("Room must be selected for all slots");
            if (!isValidTime(slot.startTime()) || !isValidTime(slot.endTime()))
                throw new IllegalArgumentException(
                        "Time must be in HH:mm format for all slots");
            if (toMinutes(slot.startTime()) >= toMinutes(slot.endTime()))
                throw new IllegalArgumentException(
                        "Start time must be before end time for all slots");

            candidates.add(new Schedule(
                    UUID.randomUUID().toString(),
                    slot.day(), slot.startTime(), slot.endTime(), slot.roomId()));
        }

        // ── Conflict checks ───────────────────────────────────────────────────
        List<Schedule> allSchedules = ScheduleRepository.getAll();
        List<Section> allSections   = SectionRepository.getAll();

        for (Schedule candidate : candidates) {
            for (Schedule existing : allSchedules) {
                // Room conflict
                if (candidate.hasConflict(existing)) {
                    throw new IllegalStateException(
                            "Room conflict on " + candidate.getDay()
                                    + " " + candidate.getStartTime()
                                    + "–" + candidate.getEndTime()
                                    + ": room already booked");
                }
            }

            // Faculty conflict — check all sections assigned to this faculty
            for (Section section : allSections) {
                if (!section.getFacultyId().equals(facultyId)) continue;
                for (String sid : section.getScheduleIds()) {
                    Schedule fs = ScheduleRepository.getById(sid);
                    if (fs != null && candidate.hasConflict(fs)) {
                        throw new IllegalStateException(
                                "Faculty conflict on " + candidate.getDay()
                                        + " " + candidate.getStartTime()
                                        + "–" + candidate.getEndTime()
                                        + ": faculty already assigned to another section");
                    }
                }
            }
        }

        // ── Auto-generate section code ────────────────────────────────────────
        Subject subject = SubjectRepository.getByCode(subjectCode);
        if (subject == null)
            throw new IllegalArgumentException("Subject not found: " + subjectCode);

        String sectionCode = generateSectionCode(subject.getCourse(), allSections);

        // ── Save schedules then section ───────────────────────────────────────
        List<String> scheduleIds = new ArrayList<>();
        for (Schedule s : candidates) {
            ScheduleRepository.save(s);
            scheduleIds.add(s.getId());
        }

        Section section = new Section(
                UUID.randomUUID().toString(),
                sectionCode,
                subjectCode,
                facultyId,
                scheduleIds,
                capacity,
                0
        );
        SectionRepository.save(section);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    /**
     * Updates a section and replaces its schedules.
     * Deletes old schedules, conflict-checks new ones, saves new schedules + updated section.
     */
    public static void updateSection(Section section, String facultyId,
                                     int capacity, List<ScheduleSlot> slots)
            throws IOException, InterruptedException {

        if (facultyId == null || facultyId.isBlank())
            throw new IllegalArgumentException("Faculty cannot be empty");
        if (capacity <= 0)
            throw new IllegalArgumentException("Capacity must be greater than 0");
        if (slots == null || slots.isEmpty())
            throw new IllegalArgumentException("At least one schedule slot is required");

        // Build candidates
        List<Schedule> candidates = new ArrayList<>();
        for (ScheduleSlot slot : slots) {
            if (!isValidTime(slot.startTime()) || !isValidTime(slot.endTime()))
                throw new IllegalArgumentException("Invalid time format");
            if (toMinutes(slot.startTime()) >= toMinutes(slot.endTime()))
                throw new IllegalArgumentException(
                        "Start time must be before end time");
            candidates.add(new Schedule(
                    UUID.randomUUID().toString(),
                    slot.day(), slot.startTime(), slot.endTime(), slot.roomId()));
        }

        // Conflict checks — exclude this section's own existing schedules
        List<Schedule> allSchedules = ScheduleRepository.getAll().stream()
                .filter(s -> !section.getScheduleIds().contains(s.getId()))
                .toList();

        List<Section> allSections = SectionRepository.getAll().stream()
                .filter(s -> !s.getId().equals(section.getId()))
                .toList();

        for (Schedule candidate : candidates) {
            for (Schedule existing : allSchedules) {
                if (candidate.hasConflict(existing))
                    throw new IllegalStateException(
                            "Room conflict on " + candidate.getDay()
                                    + " " + candidate.getStartTime()
                                    + "–" + candidate.getEndTime());
            }
            for (Section s : allSections) {
                if (!s.getFacultyId().equals(facultyId)) continue;
                for (String sid : s.getScheduleIds()) {
                    Schedule fs = ScheduleRepository.getById(sid);
                    if (fs != null && candidate.hasConflict(fs))
                        throw new IllegalStateException(
                                "Faculty conflict on " + candidate.getDay()
                                        + " " + candidate.getStartTime()
                                        + "–" + candidate.getEndTime());
                }
            }
        }

        // Delete old schedules
        for (String oldId : section.getScheduleIds()) {
            ScheduleRepository.delete(oldId);
        }

        // Save new schedules
        List<String> newScheduleIds = new ArrayList<>();
        for (Schedule s : candidates) {
            ScheduleRepository.save(s);
            newScheduleIds.add(s.getId());
        }

        // Save updated section
        Section updated = new Section(
                section.getId(),
                section.getSectionCode(),
                section.getSubjectCode(),
                facultyId,
                newScheduleIds,
                capacity,
                section.getCurrentCount()
        );
        SectionRepository.update(updated);
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /**
     * Deletes a section and all its associated schedules.
     * Guards against deletion if students are enrolled.
     */
    public static void deleteSection(Section section)
            throws IOException, InterruptedException {

        if (section.getCurrentCount() > 0)
            throw new IllegalStateException(
                    "Cannot delete section with enrolled students ("
                            + section.getCurrentCount() + " enrolled)");

        // Delete all associated schedules first
        for (String scheduleId : section.getScheduleIds()) {
            ScheduleRepository.delete(scheduleId);
        }

        SectionRepository.delete(section.getId());
    }

    // ── Section code generation ───────────────────────────────────────────────

    private static String generateSectionCode(String courseCode,
                                              List<Section> existingSections)
            throws IOException, InterruptedException {

        Course course = CourseRepository.getByCode(courseCode);
        String prefix = course != null ? course.getSectionPrefix() : courseCode + "-";

        // Count existing sections that start with this prefix
        long count = existingSections.stream()
                .filter(s -> s.getSectionCode() != null
                        && s.getSectionCode().startsWith(prefix))
                .count();

        return prefix + (count + 1);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean isValidTime(String time) {
        return time != null && time.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
    }

    private static int toMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    // ── Schedule slot descriptor ──────────────────────────────────────────────

    /**
     * Simple record to carry slot data from the UI to the service.
     */
    public record ScheduleSlot(
            Schedule.Day day,
            String startTime,
            String endTime,
            String roomId
    ) {}
}