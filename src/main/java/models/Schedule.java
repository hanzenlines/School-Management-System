package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Schedule {

    public enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    private final String id;
    private final Day day;
    private final String startTime;
    private final String endTime;
    private final String roomId;

   
    @JsonCreator
    public Schedule(
            @JsonProperty("id")          String id,
            @JsonProperty("day")         Day day,
            @JsonProperty("startTime")   String startTime,
            @JsonProperty("endTime")     String endTime,
            @JsonProperty("roomId")      String roomId
    ) {
        this.id = id;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.roomId = roomId;
    }


    // methods
    public boolean hasConflict(Schedule other) {
        if (this.day != other.day) {
            return false;
        }
        if (this.roomId.equalsIgnoreCase(other.roomId)) {
            return timesOverlap(this.startTime, this.endTime, other.startTime, other.endTime);
        }

        return false;
    }

    private boolean timesOverlap(String start1, String end1, String start2, String end2) {
        int s1 = toMinutes(start1);
        int e1 = toMinutes(end1);
        int s2 = toMinutes(start2);
        int e2 = toMinutes(end2);
      
        return s1 < e2 && s2 < e1;
    }

    private int toMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }
  
    public String getTimeSlot() {
        return day + " " + startTime + " - " + endTime;
    }
  
    // Getters and Setters
    public String getId() {
      return id;
    }

    public Day getDay() { 
      return day; 
    }

    public String getStartTime() { 
      return startTime; 
    }

    public String getEndTime() { 
      return endTime; 
    }

    public String getRoomId() {
      return roomId;
    }
}
