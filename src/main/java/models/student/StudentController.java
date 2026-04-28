package models.student;

import features.announcements.AnnouncementController;
import models.enums.UserType;

import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

public class StudentController {
    static Scanner scanner = new Scanner(System.in);
    public static void showDashboard(Student student) throws IOException, InterruptedException {
        // show announcements first
        System.out.println("\n--- Announcements ---");
        AnnouncementController.viewAnnouncements(UserType.STUDENT);

        // then menu loop
        boolean running = true;
        while (running) {
            System.out.println("\n--- Student Dashboard ---");
            System.out.println("[1] View Announcements");
            System.out.println("[0] Logout");

            int choice = 0;

            try {
                System.out.print("Select an option: ");
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("Invalid input! Please enter a number.");
                scanner.nextLine();
            }

            switch (choice) {
                case 0:
                    System.out.println("Logging out...");
                    running = false;
                    break;
            }
        }
    }
}
