package features.rooms;

import features.schedule.ScheduleRepository;
import models.Room;
import models.Schedule;
import models.enums.RoomType;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class RoomService {

    private RoomService() {}

    public static List<Room> getAllRooms() throws IOException, InterruptedException {
        return RoomRepository.getAll();
    }

//    public static int getSectionCountForRoom(String roomId)
//            throws IOException, InterruptedException {
//        // get all sections, count those whose schedule's roomId matches
//        // uses cached section + schedule data
//        return (int) SectionRepository.getAll()
//                .stream()
//                .filter(s -> {
//                    try {
//                        if (s.getScheduleId() == null) return false;
//                        // scheduleId lookup handled via ScheduleRepository cache
//                        // for now count sections referencing this room via schedule
//                        return false; // placeholder until ScheduleRepository exists
//                    } catch (Exception e) {
//                        return false;
//                    }
//                })
//                .count();
//    }

    public static void createRoom(String name, int capacity, RoomType type)
            throws IOException, InterruptedException {

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Room name cannot be empty");
        if (capacity <= 0)
            throw new IllegalArgumentException("Capacity must be greater than 0");

        Room room = new Room(
                UUID.randomUUID().toString(),
                name.trim(),
                capacity,
                type,
                true // active by default
        );
        RoomRepository.save(room);
    }

    public static void updateRoom(Room room, String name, int capacity,
                                  RoomType type, boolean isActive)
            throws IOException, InterruptedException {

        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Room name cannot be empty");
        if (capacity <= 0)
            throw new IllegalArgumentException("Capacity must be greater than 0");

        // create updated room since fields are final
        Room updated = new Room(
                room.getId(), name.trim(), capacity, type, isActive);
        RoomRepository.update(updated);
    }

    public static void deleteRoom(String roomId)
            throws IOException, InterruptedException {

        List<Schedule> schedules = ScheduleRepository.getByRoomId(roomId);
        if (!schedules.isEmpty())
            throw new IllegalStateException(
                    "Cannot delete room — it has " + schedules.size()
                            + " schedule(s) assigned to it. Remove them first.");

        RoomRepository.delete(roomId);
    }
}