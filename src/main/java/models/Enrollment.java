package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.Semester;
import models.enums.Status;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Enrollment {
    private final String id;
    private final String studentId;
    private final String subjectCode;
    private final String sectionId;
    private final Semester semester;
    private final String schoolYear;
    private Status status;
    private final LocalDateTime enrolledAt;

    @JsonCreator
    public Enrollment(
            @JsonProperty("id")String id,
            @JsonProperty("studentId")String studentId,
            @JsonProperty("subjectCode")String subjectCode,
            @JsonProperty("sectionId")String sectionId,
            @JsonProperty("semester")Semester semester,
            @JsonProperty("schoolYear")String schoolYear,
            @JsonProperty("status")Status status,
            @JsonProperty("enrolledAt")LocalDateTime enrolledAt
    ) {
        this.id = id;
        this.studentId = studentId;
        this.subjectCode = subjectCode;
        this.sectionId = sectionId;
        this.semester = semester;
        this.schoolYear = schoolYear;
        this.status = status;
        this.enrolledAt = enrolledAt;
    }

    public String getId() { return id; }

    public String getStudentId() { return studentId; }

    public String getSubjectCode() { return subjectCode; }

    public String getSectionId() { return sectionId; }

    public Semester getSemester() { return semester; }

    public String getSchoolYear() { return schoolYear; }

    public LocalDateTime getEnrolledAt() { return enrolledAt; }

    public Status getStatus() { return status; }

    public void setStatus(Status status) {
        if (status == null)
            throw new IllegalArgumentException("Status cannot be null");
        this.status = status;
    }
}