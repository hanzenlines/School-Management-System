package features.announcements;

import models.Account;
import models.Announcement;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class AnnouncementController {
    private static final Scanner scanner = new Scanner(System.in);

    private AnnouncementController() {}

    // shared — any role can view their announcements
    public static void viewAnnouncements(Account account)
            throws IOException, InterruptedException {

        List<Announcement> announcements = AnnouncementService.getAnnouncementsFor(account.getRole());

        if (announcements.isEmpty()) {
            System.out.println("No announcements available.");
            return;
        }

        announcements.forEach(Announcement::displayInfo);
    }

    // admin only — prompt and post
    public static void promptPostAnnouncement() throws IOException, InterruptedException {
        System.out.print("Title: ");
        String title = scanner.nextLine();

        System.out.print("Content: ");
        String content = scanner.nextLine();

        System.out.println("Category (GENERAL, ENROLLMENT, EXAM, HOLIDAY, REMINDER): ");
        Announcement.Category category = Announcement.Category.valueOf(scanner.nextLine().toUpperCase());

        System.out.println("Target Audience (ALL, STUDENTS, FACULTY, ADMIN): ");
        Announcement.TargetAudience audience = Announcement.TargetAudience.valueOf(scanner.nextLine().toUpperCase());

        try {
            AnnouncementService.postAnnouncement(title, content, category, audience);
            System.out.println("Announcement posted successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Failed: " + e.getMessage());
        }
    }

    // admin only — prompt and archive
    public static void promptArchiveAnnouncement() throws IOException, InterruptedException {
        System.out.print("Enter Announcement ID to archive: ");
        String id = scanner.nextLine();

        try {
            AnnouncementService.archiveAnnouncement(id);
            System.out.println("Announcement archived.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Failed: " + e.getMessage());
        }
    }

    // admin only — prompt and delete
    public static void promptDeleteAnnouncement() throws IOException, InterruptedException {
        System.out.print("Enter Announcement ID to delete: ");
        String id = scanner.nextLine();

        AnnouncementService.deleteAnnouncement(id);
        System.out.println("Announcement deleted.");
    }

    // admin only — prompt and edit
    public static void promptEditAnnouncement() throws IOException, InterruptedException {
        System.out.print("Enter Announcement ID to edit: ");
        String id = scanner.nextLine();

        System.out.print("New title (leave blank to keep): ");
        String title = scanner.nextLine();

        System.out.print("New content (leave blank to keep): ");
        String content = scanner.nextLine();

        System.out.print("New category (leave blank to keep): ");
        String categoryInput = scanner.nextLine();
        Announcement.Category category = categoryInput.isBlank() ? null :
                Announcement.Category.valueOf(categoryInput.toUpperCase());

        System.out.print("New target audience (leave blank to keep): ");
        String audienceInput = scanner.nextLine();
        Announcement.TargetAudience audience = audienceInput.isBlank() ? null :
                Announcement.TargetAudience.valueOf(audienceInput.toUpperCase());

        try {
            AnnouncementService.editAnnouncement(id, title, content, category, audience);
            System.out.println("Announcement updated.");
        } catch (IllegalArgumentException e) {
            System.out.println("Failed: " + e.getMessage());
        }
    }
}