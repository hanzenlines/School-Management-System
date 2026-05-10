package models.subject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.Semester;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Subject {
    private final String id;
    private final String subjectCode;
    private final String subjectName;
    private final int units;
    private final int yearLevel;
    private final Semester semester;
    private final String course;
    private List<String> prerequisites;

    @JsonCreator
    public Subject(
            @JsonProperty("id")             String id,
            @JsonProperty("subjectCode")    String subjectCode,
            @JsonProperty("subjectName")    String subjectName,
            @JsonProperty("units")          int units,
            @JsonProperty("yearLevel")      int yearLevel,
            @JsonProperty("semester")       Semester semester,
            @JsonProperty("course")         String course,
            @JsonProperty("prerequisites")  List<String> prerequisites
    ) {
        this.id = id;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.units = units;
        this.yearLevel = yearLevel;
        this.semester = semester;
        this.course = course;
        this.prerequisites = prerequisites;
    }

    public String getId() { return id; }

    public String getSubjectCode() { return subjectCode; }

    public String getSubjectName() { return subjectName; }

    public int getUnits() { return units; }

    public int getYearLevel() { return yearLevel; }

    public Semester getSemester() { return semester; }

    public String getCourse() { return course; }

    public List<String> getPrerequisites() { return prerequisites; }

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites != null ? prerequisites : new ArrayList<>();
    }
}
 