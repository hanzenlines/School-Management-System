package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.Semester;

import java.time.LocalDateTime;

public class EnrollmentPeriod {
    private final String id;
    private final Semester semester;
    private final String schoolYear;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private boolean isOpen;

    @JsonCreator
    public EnrollmentPeriod(
            @JsonProperty("id")          String id,
            @JsonProperty("semester")    Semester semester,
            @JsonProperty("schoolYear")  String schoolYear,
            @JsonProperty("start")       LocalDateTime start,
            @JsonProperty("end")         LocalDateTime end,
            @JsonProperty("isOpen")      boolean isOpen
    ) {
        this.id = id;
        this.semester = semester;
        this.schoolYear = schoolYear;
        this.start = start;
        this.end = end;
        this.isOpen = isOpen;
    }

    public String getId() { return id; }

    public Semester getSemester() { return semester; }

    public String getSchoolYear() { return schoolYear; }

    public LocalDateTime getStart() { return start; }

    public LocalDateTime getEnd() { return end; }

    public boolean isOpen() { return isOpen; }

    public void setOpen() {
        this.isOpen = !isOpen;
    }
}
