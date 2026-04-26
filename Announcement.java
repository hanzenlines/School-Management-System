import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Announcement {

    // meant for
    public enum TargetAudience {
        ALL, STUDENTS, FACULTY, ADMIN
    }

    public enum Category {
        GENERAL, ENROLLMENT, EXAM, HOLIDAY, REMINDER
    }

    private String announcementId;
    private String title;
    private String content;
    private Category category;
    private TargetAudience targetAudience;
    private LocalDateTime postedAt;

    // to check if active or archived
    private boolean isArchived;

    // constructor
    public Announcement(String announcementId, String title, String content,
                        Category category, TargetAudience targetAudience) {
        this.announcementId = announcementId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.targetAudience = targetAudience;
        this.postedAt = LocalDateTime.now(); // auto-set when created
        this.isArchived = false;
    }


    // methods

    // makes the announcement visible to see
    public void publish() {
        if (isArchived) {
            System.out.println("[Announcement] Can't publish an archived announcement. Make a new one.");
            return;
        }
        System.out.println("[Announcement] \"" + title + "\" is now published.");
    }

    // hides the announcement
    public void archive() {
        if (isArchived) {
            System.out.println("[Announcement] Already archived.");
            return;
        }
        isArchived = true;
        System.out.println("[Announcement] \"" + title + "\" has been archived.");
    }

    // checks who specifically should see
    public boolean isVisibleTo(TargetAudience audience) {
        return this.targetAudience == TargetAudience.ALL || this.targetAudience == audience;
    }

    public void displayInfo() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

        System.out.println("========================================");
        System.out.println("  [" + category + "] " + title);
        System.out.println("  Posted   : " + postedAt.format(formatter));
        System.out.println("  For      : " + targetAudience);
        System.out.println("  Status   : " + (isArchived ? "ARCHIVED" : "ACTIVE"));
        System.out.println("----------------------------------------");
        System.out.println("  " + content);
        System.out.println("========================================");
    }

    // getters and setters
    public String getAnnouncementId() { 
      return announcementId; 
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
}
