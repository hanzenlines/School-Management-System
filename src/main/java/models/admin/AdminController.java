package models.admin;

import features.announcements.AnnouncementController;
import models.enums.UserType;
import models.student.Student;

import java.io.IOException;
import java.util.Scanner;

public class AdminController {
    static Scanner scanner = new Scanner(System.in);
    public static void showDashboard(Admin admin) throws IOException, InterruptedException {
        // show announcements first
        System.out.println("\n--- Announcements ---");
        AnnouncementController.viewAnnouncements(UserType.ADMIN);

        // then menu loop
        boolean running = true;
        while (running) {
            System.out.println("\n--- Admin Dashboard ---");
            System.out.println("[1] View Announcements");

            String choice = scanner.nextLine();
            switch (choice) {

            }
        }
    }
}
