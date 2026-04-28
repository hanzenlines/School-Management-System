package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Section {
    private final String id;
    private final String subjectCode;
    private final String facultyId;
    private final String schedule;
    private String roomNumber;
    private int capacity;
    private int currentCount;

    @JsonCreator
    public Section(
            @JsonProperty("id")      String id,
            @JsonProperty("subjectCode")    String subjectCode,
            @JsonProperty("facultyId")      String facultyId,
            @JsonProperty("schedule")       String schedule,
            @JsonProperty("roomNumber")     String roomNumber,
            @JsonProperty("capacity")       int capacity,
            @JsonProperty("currentCount")   int currentCount
    ) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.facultyId = facultyId;
        this.schedule = schedule;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.currentCount = currentCount;
    }

    public String getSectionId() { return id; }

    public String getSubjectCode() { return subjectCode; }

    public String getFacultyId() { return facultyId; }

    public String getSchedule() { return schedule; }

    public String getRoomNumber() { return roomNumber; }

    public void setRoomNumber(String roomNumber) {
        if (roomNumber == null || roomNumber.isBlank())
            throw new IllegalArgumentException("Name cannot be empty");
        this.roomNumber = roomNumber;
    }

    public int getCapacity() { return capacity; }

    public void setCapacity(int capacity) {
        if (capacity <= 0 || capacity > 50) {
            throw new IllegalArgumentException("Invalid capacity size");
        }
        this.capacity = capacity;
    }

    public int getCurrentCount() { return currentCount; }

    public void incrementCurrentCount() {
        this.currentCount += 1;
    }

    public void decrementCurrentCount() {
        this.currentCount -= 1;
    }
}
