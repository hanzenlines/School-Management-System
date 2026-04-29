package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Announcement {

    // meant for
    public enum TargetAudience {
        ALL, STUDENTS, FACULTY, ADMIN
    }

    public enum Category {
        GENERAL, ENROLLMENT, EXAM, HOLIDAY, REMINDER
    }

    private final String id;
    private String title;
    private String content;
    private Category category;
    private TargetAudience targetAudience;
    private final LocalDateTime postedAt;

    // to check if active or archived
    private boolean isArchived;

    // constructor
    @JsonCreator
    public Announcement(
            @JsonProperty("id")              String id,
            @JsonProperty("title")           String title,
            @JsonProperty("content")         String content,
            @JsonProperty("category")        Category category,
            @JsonProperty("targetAudience")  TargetAudience targetAudience,
            @JsonProperty("postedAt")        LocalDateTime postedAt,
            @JsonProperty("isArchived")      boolean isArchived
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.category = category;
        this.targetAudience = targetAudience;
        this.postedAt = postedAt != null ? postedAt : LocalDateTime.now();
        this.isArchived = isArchived;
    }


    // getters and setters

    // checks who specifically should see
    public boolean isVisibleTo(TargetAudience audience) {
        return this.targetAudience == TargetAudience.ALL || this.targetAudience == audience;
    }

    public String getId() {
      return id;
    }

    public String getTitle() { 
      return title; 
    }

    public void setTitle(String title) { 
      this.title = title; 
    }

    public String getContent() { 
      return content; 
    }

    public void setContent(String content) { 
      this.content = content; 
    }

    public Category getCategory() { 
      return category; 
    }

    public void setCategory(Category category) {
      this.category = category; 
    }

    public TargetAudience getTargetAudience() { 
      return targetAudience; 
    }

    public void setTargetAudience(TargetAudience targetAudience) { 
      this.targetAudience = targetAudience; 
    }

    public LocalDateTime getPostedAt() { 
      return postedAt;
    }

    public boolean isArchived() { return isArchived; }

    public void archive() {
        this.isArchived = !isArchived;
    }
}
