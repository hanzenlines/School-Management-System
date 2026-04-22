public class Schedule {

    public enum Day {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    private String scheduleId;
    private Day day;
    private String startTime;   
    private String endTime;     
    private String room;

   
    // constructor
    public Schedule(String scheduleId, Day day, String startTime, String endTime, String room) {
        this.scheduleId = scheduleId;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.room = room;
    }


    // methods
    public boolean hasConflict(Schedule other) {
        if (this.day != other.day) {
            return false;
        }
        if (this.room.equalsIgnoreCase(other.room)) {
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
        return day + " " + startTime + " - " + endTime + " @ " + room;
    }

    public void displayInfo() {
        System.out.println("  Day      : " + day);
        System.out.println("  Time     : " + startTime + " - " + endTime);
        System.out.println("  Room     : " + room);
    }
  
    // Getters and Setters
    public String getScheduleId() { 
      return scheduleId; 
    }

    public Day getDay() { 
      return day; 
    }

    public void setDay(Day day) { 
      this.day = day; 
    }

    public String getStartTime() { 
      return startTime; 
    }

    public void setStartTime(String startTime) { 
      this.startTime = startTime; 
    }

    public String getEndTime() { 
      return endTime; 
    }

    public void setEndTime(String endTime) { 
      this.endTime = endTime; 
    }

    public String getRoom() { 
      return room; 
    }

    public void setRoom(String room) { 
      this.room = room; 
    }
}
