package models.student;

import features.announcements.AnnouncementService;
import features.enrollment.EnrollmentController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Announcement;
import models.enums.UserType;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentController {

    @FXML private Label studentNameLabel;
    @FXML private Label pageTitleLabel;
    @FXML private VBox contentArea;
    @FXML private Button sidebarAnnouncements;
    @FXML private Button sidebarEnrollment;

    private Student student;

    // called from Main/AuthController after login
    public static void loadDashboard(Student student) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                StudentController.class.getResource("/student_dashboard.fxml"));
        Scene scene = new Scene(loader.load());

        StudentController controller = loader.getController();
        controller.initData(student);

        Stage stage = new Stage();
        stage.setTitle("Student Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public void initData(Student student) {
        this.student = student;
        studentNameLabel.setText(student.getName());
        showAnnouncements(); // show announcements on load
    }

    @FXML
    private void showAnnouncements() {
        pageTitleLabel.setText("Announcements");
        contentArea.getChildren().clear();

        new Thread(() -> {
            try {
                List<Announcement> announcements =
                        AnnouncementService.getAnnouncementsFor(UserType.STUDENT);

                Platform.runLater(() -> {
                    if (announcements.isEmpty()) {
                        Label empty = new Label("No announcements available.");
                        empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                        return;
                    }

                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

                    for (Announcement a : announcements) {
                        VBox card = new VBox(6);
                        card.setStyle(
                                "-fx-background-color: white;" +
                                        "-fx-border-color: #e0ded8;" +
                                        "-fx-border-width: 0.5;" +
                                        "-fx-border-radius: 8;" +
                                        "-fx-background-radius: 8;" +
                                        "-fx-padding: 16;"
                        );

                        Label category = new Label(
                                a.getCategory().toString() + " · " +
                                        a.getPostedAt().format(formatter));
                        category.setStyle(
                                "-fx-font-size: 11px; -fx-text-fill: #888780;");

                        Label title = new Label(a.getTitle());
                        title.setStyle(
                                "-fx-font-size: 15px; -fx-font-weight: 500; " +
                                        "-fx-text-fill: #2c2c2a;");

                        Label content = new Label(a.getContent());
                        content.setStyle(
                                "-fx-font-size: 13px; -fx-text-fill: #5f5e5a;");
                        content.setWrapText(true);

                        card.getChildren().addAll(category, title, content);
                        contentArea.getChildren().add(card);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load announcements.");
                    error.setStyle("-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(error);
                });
            }
        }).start();
    }

    @FXML
    private void handleLogout() throws IOException {
        Stage stage = (Stage) studentNameLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/login.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("School Management System");
    }

    @FXML
    private void showEnrollment() {
        if ("Enrollment".equals(pageTitleLabel.getText())) return;
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/EnrollmentView.fxml"));

            Scene scene = new Scene(loader.load());

            EnrollmentController controller = loader.getController();
            controller.initData(student);

            Stage stage = (Stage) studentNameLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Enrollment");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static final String SIDEBAR_ACTIVE =
            "-fx-background-color: #444441; -fx-text-fill: white; -fx-font-size: 13px; " +
                    "-fx-background-radius: 6; -fx-padding: 8 12; -fx-alignment: CENTER-LEFT; -fx-cursor: hand;";

    private static final String SIDEBAR_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #888780; -fx-font-size: 13px; " +
                    "-fx-background-radius: 6; -fx-padding: 8 12; -fx-alignment: CENTER-LEFT; -fx-cursor: hand;";

    private void setSidebarActive(Button active) {
        sidebarAnnouncements.setStyle(SIDEBAR_INACTIVE);
        sidebarEnrollment.setStyle(SIDEBAR_INACTIVE);
        active.setStyle(SIDEBAR_ACTIVE);
    }
}