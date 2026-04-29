package models.faculty;

import features.announcements.AnnouncementService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Announcement;
import models.enums.UserType;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FacultyController {

    @FXML private Label facultyNameLabel;
    @FXML private Label pageTitleLabel;
    @FXML private VBox contentArea;

    private Faculty faculty;

    // called from Main/AuthController after login
    public static void loadDashboard(Faculty faculty) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                models.faculty.FacultyController.class.getResource("/faculty_dashboard.fxml"));
        Scene scene = new Scene(loader.load());

        models.faculty.FacultyController controller = loader.getController();
        controller.initData(faculty);

        Stage stage = new Stage();
        stage.setTitle("Faculty Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public void initData(Faculty faculty) {
        this.faculty = faculty;
        facultyNameLabel.setText(faculty.getName());
        showAnnouncements(); // show announcements on load
    }

    @FXML
    private void showAnnouncements() {
        pageTitleLabel.setText("Announcements");
        contentArea.getChildren().clear();

        new Thread(() -> {
            try {
                List<Announcement> announcements =
                        AnnouncementService.getAnnouncementsFor(UserType.FACULTY);

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
        Stage stage = (Stage) facultyNameLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/login.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("School Management System");
    }
}