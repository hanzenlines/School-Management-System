package models.student;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.Account;
import models.enums.UserType;

public class Student extends Account {
    private final String studentNumber;
    private String course;
    private int yearLevel;
    private boolean hasPendingBalance;

    @JsonCreator
    public Student(
            @JsonProperty("id")            String id,
            @JsonProperty("name")          String name,
            @JsonProperty("email")         String email,
            @JsonProperty("password")      String password,
            @JsonProperty("contactNumber") String contactNumber,
            @JsonProperty("studentNumber") String studentNumber,
            @JsonProperty("course")        String course,
            @JsonProperty("yearLevel")     int yearLevel,
            @JsonProperty("hasPendingBalance")  boolean hasPendingBalance
    ) {
        super(id, name, email, password, contactNumber, UserType.STUDENT);
        this.studentNumber = studentNumber;
        setCourse(course);
        setYearLevel(yearLevel);
    }

    public String getStudentNumber() { return studentNumber; }
    public String getCourse() { return course; }
    public int getYearLevel() { return yearLevel; }

    public void setCourse(String course) {
        if (course == null || course.isBlank())
            throw new IllegalArgumentException("Course cannot be empty");
        this.course = course;
    }

    public void setYearLevel(int yearLevel) {
        if (yearLevel < 1 || yearLevel > 5)
            throw new IllegalArgumentException("Year level must be between 1 and 5");
        this.yearLevel = yearLevel;
    }

    public void togglePendingBalance() {
        this.hasPendingBalance = !hasPendingBalance;
    }
}
