package models.subject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.Semester;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CompletedSubject {
    private final String id;
    private final String studentId;
    private final String subjectCode;
    private double grade;
    private final Semester semester;
    private final String schoolYear;

    @JsonCreator
    public CompletedSubject(
            @JsonProperty("id") String id,
            @JsonProperty("studentId") String studentId,
            @JsonProperty("subjectCode") String subjectCode,
            @JsonProperty("grade") double grade,
            @JsonProperty("semester") Semester semester,
            @JsonProperty("schoolYear") String schoolYear
    ) {
        this.id = id;
        this.studentId = studentId;
        this.subjectCode = subjectCode;
        this.grade = grade;
        this.semester = semester;
        this.schoolYear = schoolYear;
    }
    public String getId() { return id; }

    public String getStudentId() { return studentId; }

    public String getSubjectCode() { return subjectCode; }

    public Semester getSemester() { return semester; }

    public String getSchoolYear() { return schoolYear; }

    public double getGrade() { return grade; }

    public void setGrade(double grade) {
        if (grade < 1.0 || grade > 5.0)
            throw new IllegalArgumentException("Grade must be between 1.0 and 5.0");
        this.grade = grade;
    }
}