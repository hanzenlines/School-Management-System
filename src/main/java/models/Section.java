//package models;
//
//import models.faculty.Faculty;
//
//public class Section {
//
//    private String sectionId;
//    private String sectionCode;
//    private int maxSlots;
//    private int currentSlots;
//    private String remarks;
//    private String semester;
//    private String schoolYear;
//
//
//    private Subject subject;
//    private Faculty faculty;
//    private Schedule schedule;
//
//    // constructor
//    public Section(String sectionId, String sectionCode, int maxSlots,
//                   String semester, String schoolYear) {
//        this.sectionId = sectionId;
//        this.sectionCode = sectionCode;
//        this.maxSlots = maxSlots;
//        this.currentSlots = 0;
//        this.semester = semester;
//        this.schoolYear = schoolYear;
//        this.remarks = "";
//        this.subject = null;
//        this.faculty = null;
//        this.schedule = null;
//    }
//
//
//    // methods
//    public int getAvailableSlots() {
//        return maxSlots - currentSlots;
//    }
//
//    public boolean isFull() {
//        return currentSlots >= maxSlots;
//    }
//
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
//            System.out.println("[Section] No students in " + sectionCode + " to remove.");
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
//
//    // Getters and Setters
//    public String getSectionId() {
//      return sectionId;
//    }
//
//    public String getSectionCode() {
//      return sectionCode;
//    }
//
//    public int getMaxSlots() {
//      return maxSlots;
//    }
//
//    public void setMaxSlots(int maxSlots) {
//        if (maxSlots < currentSlots) {
//            System.out.println("[Section] Can't set max below current enrolled count.");
//            return;
//        }
//        this.maxSlots = maxSlots;
//    }
//
//    public int getCurrentSlots() {
//      return currentSlots;
//    }
//
//    public String getRemarks() {
//      return remarks;
//    }
//
//    public void setRemarks(String remarks) {
//      this.remarks = remarks;
//    }
//
//    public String getSemester() {
//      return semester;
//    }
//
//    public String getSchoolYear() {
//      return schoolYear;
//    }
//
//    public Subject getSubject() {
//      return subject;
//    }
//
//    public Faculty getFaculty() {
//      return faculty;
//    }
//
//    public Schedule getSchedule() {
//      return schedule;
//    }
//}
