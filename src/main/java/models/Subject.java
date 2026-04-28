package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.Semester;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Subject {
    private final String subjectCode;
    private final String subjectName;
    private final int units;
    private final int yearLevel;
    private final Semester semester;
    private final String course;
    private List<String> prerequisites;

    @JsonCreator
    public Subject(
            @JsonProperty("prerequisites")  List<String> prerequisites,
            @JsonProperty("course")         String course,
            @JsonProperty("semester")       Semester semester,
            @JsonProperty("yearLevel")      int yearLevel,
            @JsonProperty("units")          int units,
            @JsonProperty("subjectName")    String subjectName,
            @JsonProperty("subjectCode")    String subjectCode
    ) {
        this.prerequisites = prerequisites;
        this.course = course;
        this.semester = semester;
        this.yearLevel = yearLevel;
        this.units = units;
        this.subjectName = subjectName;
        this.subjectCode = subjectCode;
    }

    public String getSubjectCode() { return subjectCode; }

    public String getSubjectName() { return subjectName; }

    public int getUnits() { return units; }

    public int getYearLevel() { return yearLevel; }

    public Semester getSemester() { return semester; }

    public String getCourse() { return course; }

    public List<String> getPrerequisites() { return prerequisites; }

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites;
    }
}
