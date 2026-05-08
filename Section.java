package models.section;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
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
            @JsonProperty("id")             String id,
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


    // methods
    public int getAvailableSlots() {
        return capacity - currentCount;
    }

    public boolean isFull() {
        return currentCount >= capacity;
    }

//    public boolean addStudent() {
//        if (isFull()) {
//            System.out.println("[Section] " + sectionCode + " is already full.");
//            return false;
//        }
//        currentSlots++;
//        System.out.println("[Section] Student added to " + sectionCode
//                + ". Slots: " + currentSlots + "/" + maxSlots);
//        return true;
//    }
//
//    public boolean removeStudent() {
//        if (currentSlots <= 0) {
//            System.out.println("[models.section.Section] No students in " + sectionCode + " to remove.");
//            return false;
//        }
//        currentSlots--;
//        System.out.println("[Section] Student removed from " + sectionCode
//                + ". Slots: " + currentSlots + "/" + maxSlots);
//        return true;
//    }
//
//    public void assignFaculty(Faculty faculty) {
//        this.faculty = faculty;
//        if (this.subject != null) {
//            faculty.addSubject(this.subject.getTitle());
//        }
//
//        System.out.println("[Section] Faculty " + faculty.getEmployeeId()
//                + " assigned to " + sectionCode);
//    }
//
//    public void assignSubject(Subject subject) {
//        this.subject = subject;
//        System.out.println("[Section] Subject " + subject.getSubjectCode()
//                + " - " + subject.getTitle() + " assigned to " + sectionCode);
//    }
//
//    public void assignSchedule(Schedule schedule) {
//        this.schedule = schedule;
//        System.out.println("[Section] Schedule assigned to " + sectionCode);
//    }
//
//    public void displayInfo() {
//        System.out.println("========================================");
//        System.out.println("  SECTION INFO");
//        System.out.println("========================================");
//        System.out.println("  Section ID   : " + sectionId);
//        System.out.println("  Section Code : " + sectionCode);
//
//        if (subject != null) {
//            System.out.println("  Subject      : " + subject.getSubjectCode()
//                    + " - " + subject.getTitle()
//                    + " (" + subject.getUnits() + " units)");
//        } else {
//            System.out.println("  Subject      : Not assigned yet");
//        }
//
//        if (faculty != null) {
//            System.out.println("  Faculty      : " + faculty.getEmployeeId()
//                    + " | Dept: " + faculty.getDepartment());
//        } else {
//            System.out.println("  Faculty      : TBA");
//        }
//
//        System.out.println("  Semester     : " + semester + "  |  S.Y. " + schoolYear);
//        System.out.println("  Slots        : " + currentSlots + " / " + maxSlots
//                + "  (" + getAvailableSlots() + " available)");
//        System.out.println("  Status       : " + (isFull() ? "FULL" : "OPEN"));
//
//        if (!remarks.isEmpty()) {
//            System.out.println("  Remarks      : " + remarks);
//        }
//
//        if (schedule != null) {
//            System.out.println("  --- Schedule ---");
//            schedule.displayInfo();
//        } else {
//            System.out.println("  Schedule     : Not set yet");
//        }
//
//        System.out.println("========================================");
//    }

    // Getters and Setters
    public String getId() { return id; }

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
 
