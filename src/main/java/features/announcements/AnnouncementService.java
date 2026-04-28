package features.announcements;

import models.Announcement;
import models.enums.UserType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class AnnouncementService {

    private AnnouncementService() {}

    // filters announcements by target audience based on the logged-in role
    public static List<Announcement> getAnnouncementsFor(UserType userType)
            throws IOException, InterruptedException {

        Announcement.TargetAudience audience = switch (userType) {
            case STUDENT -> Announcement.TargetAudience.STUDENTS;
            case FACULTY -> Announcement.TargetAudience.FACULTY;
            case ADMIN   -> Announcement.TargetAudience.ADMIN;
        };

        return AnnouncementRepository.getAll()
                .stream()
                .filter(a -> !a.isArchived())
                .filter(a -> a.isVisibleTo(audience))
                .collect(Collectors.toList());
    }

    // ADMIN only — post a new announcement
    public static void postAnnouncement(String title, String content,
                                        Announcement.Category category,
                                        Announcement.TargetAudience targetAudience)
            throws IOException, InterruptedException {

        if (title == null || title.isBlank())
            throw new IllegalArgumentException("Title cannot be empty");
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("Content cannot be empty");

        String id = "ann-" + System.currentTimeMillis(); // simple id generation
        Announcement announcement = new Announcement(id, title, content, category, targetAudience, LocalDateTime.now(), false);
        AnnouncementRepository.post(announcement);
    }

    // ADMIN only — archive instead of hard delete (soft delete)
    public static void archiveAnnouncement(String announcementId) throws IOException, InterruptedException {

        List<Announcement> all = AnnouncementRepository.getAll();

        Announcement target = all.stream()
                .filter(a -> a.getAnnouncementId().equals(announcementId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("models.Announcement not found"));

        if (target.isArchived())
            throw new IllegalStateException("models.Announcement is already archived");

        target.archive(); // flips isArchived to true
        AnnouncementRepository.update(target); // persist the change
    }

    // ADMIN only — hard delete
    public static void deleteAnnouncement(String announcementId) throws IOException, InterruptedException {
        AnnouncementRepository.delete(announcementId);
    }

    // ADMIN only — edit an existing announcement
    public static void editAnnouncement(String announcementId, String newTitle,
                                        String newContent, Announcement.Category newCategory,
                                        Announcement.TargetAudience newAudience)
            throws IOException, InterruptedException {

        List<Announcement> all = AnnouncementRepository.getAll();

        Announcement target = all.stream()
                .filter(a -> a.getAnnouncementId().equals(announcementId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("models.Announcement not found"));

        if (newTitle != null && !newTitle.isBlank()) target.setTitle(newTitle);
        if (newContent != null && !newContent.isBlank()) target.setContent(newContent);
        if (newCategory != null) target.setCategory(newCategory);
        if (newAudience != null) target.setTargetAudience(newAudience);

        AnnouncementRepository.update(target);
    }
}
