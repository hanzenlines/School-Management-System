package models;

import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.Semester;
import models.enums.Status;

public class Enrollment {
    private final String enrollmentId;
    private final String studentId;
    private final String subjectCode;
    private final String sectionId;
    private final Semester semester;
    private final String schoolYear;
    private Status status;
    private final String enrolledAt;

    public Enrollment(
            @JsonProperty("enrolledAt")         String enrolledAt,
            @JsonProperty("status")             Status status,
            @JsonProperty("schoolYear")         String schoolYear,
            @JsonProperty("semester")           Semester semester,
            @JsonProperty("sectionId")          String sectionId,
            @JsonProperty("subjectCode")        String subjectCode,
            @JsonProperty("studentId")          String studentId,
            @JsonProperty("enrollmentId")       String enrollmentId
    ) {
        this.enrolledAt = enrolledAt;
        this.status = status;
        this.schoolYear = schoolYear;
        this.semester = semester;
        this.sectionId = sectionId;
        this.subjectCode = subjectCode;
        this.studentId = studentId;
        this.enrollmentId = enrollmentId;
    }

    public String getEnrollmentId() { return enrollmentId; }

    public String getStudentId() { return studentId; }

    public String getSubjectCode() { return subjectCode; }

    public String getSectionId() { return sectionId; }

    public Semester getSemester() { return semester; }

    public String getSchoolYear() { return schoolYear; }

    public String getEnrolledAt() { return enrolledAt; }

    public Status getStatus() { return status; }
}
