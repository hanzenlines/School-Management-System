package features.schedule;

import models.Schedule;
import models.section.Section;
import models.section.SectionRepository;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class ScheduleService {

    private ScheduleService() {}

    public static List<Schedule> getAllSchedules()
            throws IOException, InterruptedException {
        return ScheduleRepository.getAll();
    }

    public static Schedule getScheduleById(String id)
            throws IOException, InterruptedException {
        return ScheduleRepository.getById(id);
    }

    /**
     * Creates a new schedule after checking for room conflicts on the same day/time.
     * Throws IllegalStateException if a conflict is found.
     */
    public static void createSchedule(Schedule.Day day, String startTime,
                                      String endTime, String roomId)
            throws IOException, InterruptedException {

        if (startTime == null || startTime.isBlank())
            throw new IllegalArgumentException("Start time cannot be empty");
        if (endTime == null || endTime.isBlank())
            throw new IllegalArgumentException("End time cannot be empty");
        if (roomId == null || roomId.isBlank())
            throw new IllegalArgumentException("Room must be selected");
        if (!isValidTimeFormat(startTime) || !isValidTimeFormat(endTime))
            throw new IllegalArgumentException("Time must be in HH:mm format");
        if (toMinutes(startTime) >= toMinutes(endTime))
            throw new IllegalArgumentException("Start time must be before end time");

        Schedule candidate = new Schedule(
                UUID.randomUUID().toString(), day, startTime, endTime, roomId);

        // Conflict check — same room, same day, overlapping time
        List<Schedule> existing = ScheduleRepository.getAll();
        for (Schedule s : existing) {
            if (candidate.hasConflict(s)) {
                throw new IllegalStateException(
                        "Schedule conflicts with an existing one: "
                                + s.getTimeSlot());
            }
        }

        ScheduleRepository.save(candidate);
    }

    /**
     * Deletes a schedule.
     * Guard against deletion if a section references this schedule
     * will be wired in once SectionRepository is updated.
     */
    public static void deleteSchedule(String scheduleId)
            throws IOException, InterruptedException {

        List<Section> sections = SectionRepository.getByScheduleId(scheduleId);
        if (!sections.isEmpty())
            throw new IllegalStateException(
                    "Cannot delete schedule — it is referenced by section: "
                            + sections.get(0).getSectionCode() + ". Delete the section first.");

        ScheduleRepository.delete(scheduleId);
    }

    public static void updateSchedule(Schedule schedule, Schedule.Day day,
                                      String startTime, String endTime, String roomId)
            throws IOException, InterruptedException {

        if (startTime == null || startTime.isBlank())
            throw new IllegalArgumentException("Start time cannot be empty");
        if (endTime == null || endTime.isBlank())
            throw new IllegalArgumentException("End time cannot be empty");
        if (!isValidTimeFormat(startTime) || !isValidTimeFormat(endTime))
            throw new IllegalArgumentException("Time must be in HH:mm format");
        if (toMinutes(startTime) >= toMinutes(endTime))
            throw new IllegalArgumentException("Start time must be before end time");

        Schedule updated = new Schedule(
                schedule.getId(), day, startTime, endTime, roomId);

        // Conflict check — exclude self
        List<Schedule> existing = ScheduleRepository.getAll();
        for (Schedule s : existing) {
            if (s.getId().equals(schedule.getId())) continue; // skip self
            if (updated.hasConflict(s)) {
                throw new IllegalStateException(
                        "Schedule conflicts with an existing one: "
                                + s.getTimeSlot());
            }
        }

        ScheduleRepository.update(updated);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static boolean isValidTimeFormat(String time) {
        return time.matches("^([01]\\d|2[0-3]):[0-5]\\d$");
    }

    private static int toMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
}