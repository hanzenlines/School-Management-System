import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;


public class Main extends Application {

    private static Process jsonServerProcess;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("School Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
//        startJsonServer();
        launch(args);
    }

//    private static void startJsonServer() {
//        try {
//            ProcessBuilder pb = System.getProperty("os.name").toLowerCase().contains("win")
//                    ? new ProcessBuilder("cmd", "/c", "npx", "json-server", "data.json")
//                    : new ProcessBuilder("npx", "json-server", "data.json");
//            pb.directory(new File(System.getProperty("user.dir")));
//            pb.redirectErrorStream(true);
//            jsonServerProcess = pb.start();
//
//            // shutdown hook so it stops when the app closes
//            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//                if (jsonServerProcess != null && jsonServerProcess.isAlive()) {
//                    jsonServerProcess.destroy();
//                }
//            }));
//
//        } catch (Exception e) {
//            System.err.println("Failed to start json-server: " + e.getMessage());
//        }
//    }
}


//public class Main {
//    public static void main(String[] args) throws IOException, InterruptedException {
//        final Scanner sc = new Scanner(System.in);
//        while (true) {
//            int choice = -1;
//            System.out.println("\n--- Main Menu ---");
//            System.out.println("[1] Log in");
//            System.out.println("[0] Exit");
//
//            try {
//                System.out.print("Select Option: ");
//                choice = sc.nextInt();
//            } catch (InputMismatchException e) {
//                System.out.println("Invalid input. Please enter a number.");
//                sc.nextLine(); // discard the bad input
//            }
//
//            switch (choice) {
//                case 1 -> {
//                    Account account = null;
//                    while (account == null) {
//                        account = AuthController.promptLogin();
//                    }
//
//                    switch (account.getUserType()) {
//                        case STUDENT -> StudentController.showDashboard((Student) account);
//                        case FACULTY -> FacultyController.showDashboard((Faculty) account);
//                        case ADMIN   -> AdminController.showDashboard((Admin) account);
//                    }
//                }
//                case 0 -> System.exit(0);
//            }
//        }
//    }
//}