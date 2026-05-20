package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.Semester;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Grade {
    private final String id;
    private final String studentId;
    private final String sectionId;
    private final String subjectCode;
    private final Semester semester;
    private final String schoolYear;
    private Double prelimGrade;
    private Double midtermGrade;
    private Double preFinalGrade;
    private Double finalGrade;
    private Double computedMidterm;
    private Double computedFinal;
    private Double overallGrade;
    private LocalDateTime submittedAt;

    @JsonCreator
    public Grade(
            @JsonProperty("id")             String id,
            @JsonProperty("studentId")      String studentId,
            @JsonProperty("sectionId")      String sectionId,
            @JsonProperty("subjectCode")    String subjectCode,
            @JsonProperty("semester")       Semester semester,
            @JsonProperty("schoolYear")     String schoolYear,
            @JsonProperty("prelimGrade")    Double prelimGrade,
            @JsonProperty("midtermGrade")   Double midtermGrade,
            @JsonProperty("preFinalGrade")  Double preFinalGrade,
            @JsonProperty("finalGrade")     Double finalGrade,
            @JsonProperty("computedMidterm") Double computedMidterm,
            @JsonProperty("computedFinal")  Double computedFinal,
            @JsonProperty("overallGrade")   Double overallGrade,
            @JsonProperty("submittedAt")    LocalDateTime submittedAt
    ) {
        this.id = id;
        this.studentId = studentId;
        this.sectionId = sectionId;
        this.subjectCode = subjectCode;
        this.semester = semester;
        this.schoolYear = schoolYear;
        this.prelimGrade = prelimGrade;
        this.midtermGrade = midtermGrade;
        this.preFinalGrade = preFinalGrade;
        this.finalGrade = finalGrade;
        this.computedMidterm = computedMidterm;
        this.computedFinal = computedFinal;
        this.overallGrade = overallGrade;
        this.submittedAt = submittedAt;
    }

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getSectionId() { return sectionId; }
    public String getSubjectCode() { return subjectCode; }
    public Semester getSemester() { return semester; }
    public String getSchoolYear() { return schoolYear; }
    public Double getPrelimGrade() { return prelimGrade; }
    public Double getMidtermGrade() { return midtermGrade; }
    public Double getPreFinalGrade() { return preFinalGrade; }
    public Double getFinalGrade() { return finalGrade; }
    public Double getComputedMidterm() { return computedMidterm; }
    public Double getComputedFinal() { return computedFinal; }
    public Double getOverallGrade() { return overallGrade; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }

    public void setPrelimGrade(Double prelimGrade) { this.prelimGrade = prelimGrade; }
    public void setMidtermGrade(Double midtermGrade) { this.midtermGrade = midtermGrade; }
    public void setPreFinalGrade(Double preFinalGrade) { this.preFinalGrade = preFinalGrade; }
    public void setFinalGrade(Double finalGrade) { this.finalGrade = finalGrade; }
    public void setComputedMidterm(Double computedMidterm) { this.computedMidterm = computedMidterm; }
    public void setComputedFinal(Double computedFinal) { this.computedFinal = computedFinal; }
    public void setOverallGrade(Double overallGrade) { this.overallGrade = overallGrade; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}