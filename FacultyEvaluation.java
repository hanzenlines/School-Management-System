package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.Semester;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FacultyEvaluation {

    private final String id;
    private final String studentId;
    private final String facultyId;
    private final String sectionId;
    private final Semester semester;
    private final String schoolYear;

    // ── Fixed criteria (1–5) ──────────────────────────────────────────────────
    private final int teachingEffectiveness;
    private final int subjectKnowledge;
    private final int communication;
    private final int professionalism;
    private final int studentEngagement;

    private final String comments;


    // constructor
    @JsonCreator
    public FacultyEvaluation(
            @JsonProperty("id")                     String id,
            @JsonProperty("studentId")              String studentId,
            @JsonProperty("facultyId")              String facultyId,
            @JsonProperty("sectionId")              String sectionId,
            @JsonProperty("semester")               Semester semester,
            @JsonProperty("schoolYear")             String schoolYear,
            @JsonProperty("teachingEffectiveness")  int teachingEffectiveness,
            @JsonProperty("subjectKnowledge")       int subjectKnowledge,
            @JsonProperty("communication")          int communication,
            @JsonProperty("professionalism")        int professionalism,
            @JsonProperty("studentEngagement")      int studentEngagement,
            @JsonProperty("comments")               String comments
    ) {
        this.id = id;
        this.studentId = studentId;
        this.facultyId = facultyId;
        this.sectionId = sectionId;
        this.semester = semester;
        this.schoolYear = schoolYear;
        this.teachingEffectiveness = teachingEffectiveness;
        this.subjectKnowledge = subjectKnowledge;
        this.communication = communication;
        this.professionalism = professionalism;
        this.studentEngagement = studentEngagement;
        this.comments = comments != null ? comments : "";
    }

    // gets average of all ratings
    public double getAverageScore() {
        return (teachingEffectiveness + subjectKnowledge
                + communication + professionalism
                + studentEngagement) / 5.0;
    }

    // getters and Setters
    public String getId()                   { return id; }
    public String getStudentId()            { return studentId; }
    public String getFacultyId()            { return facultyId; }
    public String getSectionId()            { return sectionId; }
    public Semester getSemester()           { return semester; }
    public String getSchoolYear()           { return schoolYear; }
    public int getTeachingEffectiveness()   { return teachingEffectiveness; }
    public int getSubjectKnowledge()        { return subjectKnowledge; }
    public int getCommunication()           { return communication; }
    public int getProfessionalism()         { return professionalism; }
    public int getStudentEngagement()       { return studentEngagement; }
    public String getComments()             { return comments; }
}
