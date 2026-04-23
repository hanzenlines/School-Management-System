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
    // to indicate if showing or archived
    private boolean isArchived;

    // constructor
    public Announcement(String announcementId, String title, String content,
                        Category category, TargetAudience targetAudience) {
        this.announcementId = announcementId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.targetAudience = targetAudience;
        this.postedAt = LocalDateTime.now(); // automatically set when created
        this.isArchived = false;
    }

    // methods
    // makes the announcement visible or live
    public void publish() {
        if (isArchived) {
            System.out.println("[Announcement] Can't publish an archived announcement. Make a new one.");
            return;
        }
        System.out.println("[Announcement] \"" + title + "\" is now published.");
    }
  
    // hides announcement from the main feed
    public void archive() {
        if (isArchived) {
            System.out.println("[Announcement] Already archived.");
            return;
        }
        isArchived = true;
        System.out.println("[Announcement] \"" + title + "\" has been archived.");
    }

    // checks specifically who can see this
    public boolean isVisibleTo(TargetAudience audience) {
        return this.targetAudience == TargetAudience.ALL || this.targetAudience == audience;
    }
    
    public void displayInfo() {
        
    }
  
    // fetters and setters
    
}
