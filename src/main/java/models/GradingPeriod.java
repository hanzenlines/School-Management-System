package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.Semester;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GradingPeriod {
    private final String id;
    private final Semester semester;
    private final String schoolYear;
    private LocalDateTime deadline;
    private boolean isOpen;

    @JsonCreator
    public GradingPeriod(
            @JsonProperty("id")         String id,
            @JsonProperty("semester")   Semester semester,
            @JsonProperty("schoolYear") String schoolYear,
            @JsonProperty("deadline")   LocalDateTime deadline,
            @JsonProperty("isOpen")     boolean isOpen
    ) {
        this.id = id;
        this.semester = semester;
        this.schoolYear = schoolYear;
        this.deadline = deadline;
        this.isOpen = isOpen;
    }

    public String getId() { return id; }
    public Semester getSemester() { return semester; }
    public String getSchoolYear() { return schoolYear; }
    public LocalDateTime getDeadline() { return deadline; }
    public boolean isOpen() { return isOpen; }

    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public void setOpen(boolean open) { this.isOpen = open; }
}