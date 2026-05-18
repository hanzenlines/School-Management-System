package models.section;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Section {

    private final String id;
    private final String sectionCode;       // e.g. "IT-G1"
    private final String subjectCode;
    private final String facultyId;
    private final List<String> scheduleIds; // one per day/time slot
    private int capacity;
    private int currentCount;

    @JsonCreator
    public Section(
            @JsonProperty("id")             String id,
            @JsonProperty("sectionCode")    String sectionCode,
            @JsonProperty("subjectCode")    String subjectCode,
            @JsonProperty("facultyId")      String facultyId,
            @JsonProperty("scheduleIds")    List<String> scheduleIds,
            @JsonProperty("capacity")       int capacity,
            @JsonProperty("currentCount")   int currentCount
    ) {
        this.id = id;
        this.sectionCode = sectionCode;
        this.subjectCode = subjectCode;
        this.facultyId = facultyId;
        this.scheduleIds = scheduleIds != null ? scheduleIds : new ArrayList<>();
        this.capacity = capacity;
        this.currentCount = currentCount;
    }

    // ── Domain methods ────────────────────────────────────────────────────────

    public int getAvailableSlots() { return capacity - currentCount; }
    public boolean isFull()        { return currentCount >= capacity; }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getId()                { return id; }
    public String getSectionCode()       { return sectionCode; }
    public String getSubjectCode()       { return subjectCode; }
    public String getFacultyId()         { return facultyId; }
    public List<String> getScheduleIds() { return scheduleIds; }
    public int getCapacity()             { return capacity; }
    public int getCurrentCount()         { return currentCount; }

    // ── Setters ───────────────────────────────────────────────────────────────

    public void setCapacity(int capacity) {
        if (capacity <= 0 || capacity > 50)
            throw new IllegalArgumentException("Invalid capacity size");
        this.capacity = capacity;
    }

    public void incrementCurrentCount() {
        if (currentCount >= capacity)
            throw new IllegalStateException("Section is already full");
        this.currentCount++;
    }

    public void decrementCurrentCount() {
        if (currentCount <= 0)
            throw new IllegalStateException("Section is already empty");
        this.currentCount--;
    }
}