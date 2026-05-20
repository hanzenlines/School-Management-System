package models.admin;

import features.enrollment.EnrollmentPeriodController;
import features.evaluation.EvaluationController;
import features.evaluation.EvaluationResultsController;
import features.grades.GradingPeriodController;
import features.rooms.RoomController;
import features.schedule.ScheduleController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.faculty.FacultyListController;
import models.student.StudentListController;

import java.io.IOException;

public class AdminController {

    @FXML private Label adminNameLabel;
    @FXML private Label pageTitleLabel;
    @FXML private VBox contentArea;
    @FXML private Button sidebarAnnouncements;
    @FXML private Button sidebarRooms;
    @FXML private Button sidebarSchedules;
    @FXML private Button sidebarSections;
    @FXML private Button sidebarStudents;
    @FXML private Button sidebarFaculty;
    @FXML private Button sidebarEvaluation;
    @FXML private Button sidebarEnrollmentPeriod;
    @FXML private Button sidebarGradingPeriod;

    private Admin admin;

    private static final String SIDEBAR_ACTIVE =
            "-fx-background-color: #444441; -fx-text-fill: white; -fx-font-size: 13px; " +
                    "-fx-background-radius: 6; -fx-padding: 8 12; -fx-alignment: CENTER-LEFT; -fx-cursor: hand;";

    private static final String SIDEBAR_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #888780; -fx-font-size: 13px; " +
                    "-fx-background-radius: 6; -fx-padding: 8 12; -fx-alignment: CENTER-LEFT; -fx-cursor: hand;";

    // ── Bootstrap ─────────────────────────────────────────────────────────────

    public static void loadDashboard(Admin admin) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                AdminController.class.getResource("/admin_dashboard.fxml"));
        Scene scene = new Scene(loader.load());

        AdminController controller = loader.getController();
        controller.initData(admin);

        Stage stage = new Stage();
        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public void initData(Admin admin) {
        this.admin = admin;
        adminNameLabel.setText(admin.getName());
        setSidebarActive(sidebarAnnouncements);
        loadView("Announcements", sidebarAnnouncements,
                "/AnnouncementListView.fxml",
                controller -> ((AnnouncementController) controller).initData());
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private void setSidebarActive(Button active) {
        sidebarAnnouncements.setStyle(SIDEBAR_INACTIVE);
        sidebarRooms.setStyle(SIDEBAR_INACTIVE);
        sidebarSchedules.setStyle(SIDEBAR_INACTIVE);
//        sidebarSections.setStyle(SIDEBAR_INACTIVE);
        sidebarStudents.setStyle(SIDEBAR_INACTIVE);
        sidebarFaculty.setStyle(SIDEBAR_INACTIVE);
        sidebarEvaluation.setStyle(SIDEBAR_INACTIVE);
        sidebarEnrollmentPeriod.setStyle(SIDEBAR_INACTIVE);
        sidebarGradingPeriod.setStyle(SIDEBAR_INACTIVE);
        active.setStyle(SIDEBAR_ACTIVE);
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML
    private void showAnnouncements() {
        if ("Announcements".equals(pageTitleLabel.getText())) return;
        loadView("Announcements", sidebarAnnouncements,
                "/AnnouncementListView.fxml",
                controller -> ((AnnouncementController) controller).initData());
    }

    @FXML
    private void showRooms() {
        if ("Rooms".equals(pageTitleLabel.getText())) return;
        loadView("Rooms", sidebarRooms,
                "/RoomListView.fxml",
                controller -> ((RoomController) controller).initData());
    }

    @FXML
    private void showSchedules() {
        if ("Schedules".equals(pageTitleLabel.getText())) return;
        loadView("Schedules", sidebarSchedules,
                "/ScheduleListView.fxml",
                controller -> ((ScheduleController) controller).initData());
    }

//    @FXML
//    private void showSections() {
//        if ("Sections".equals(pageTitleLabel.getText())) return;
//        loadView("Sections", sidebarSections,
//                "/features/admin/sections/SectionListView.fxml",
//                controller -> ((SectionController) controller).initData());
//    }

    @FXML
    private void showStudents() {
        if ("Students".equals(pageTitleLabel.getText())) return;
        loadView("Students", sidebarStudents,
                "/StudentListView.fxml",
                controller -> ((StudentListController) controller).initData());
    }

    @FXML
    private void showFaculty() {
        if ("Faculty".equals(pageTitleLabel.getText())) return;
        loadView("Faculty", sidebarFaculty,
                "/FacultyListView.fxml",
                controller -> ((FacultyListController) controller).initData());
    }

    @FXML
    private void showEnrollmentPeriod() {
        if ("Enrollment Period".equals(pageTitleLabel.getText())) return;
        loadView("Enrollment Period", sidebarEnrollmentPeriod,
                "/EnrollmentPeriodView.fxml",
                controller -> ((EnrollmentPeriodController) controller).initData());
    }

    @FXML
    private void showGradingPeriod() {
        if ("Grading Period".equals(pageTitleLabel.getText())) return;
        loadView("Grading Period", sidebarGradingPeriod,
                "/GradingPeriodView.fxml",
                controller -> ((GradingPeriodController) controller).initData());
    }

    @FXML
    private void showEvaluationResults() {
        if ("Faculty Evaluation Results".equals(pageTitleLabel.getText())) return;
        loadView("Faculty Evaluation Results", sidebarEvaluation,
                "/EvaluationResultsView.fxml",
                controller -> ((EvaluationResultsController) controller).initData());

    }

    // ── Generic view loader ───────────────────────────────────────────────────

    private void loadView(String title, Button sidebarBtn,
                          String fxmlPath,
                          java.util.function.Consumer<Object> initFn) {
        setSidebarActive(sidebarBtn);
        pageTitleLabel.setText(title);
        contentArea.getChildren().clear();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            initFn.accept(loader.getController());
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            contentArea.getChildren().add(buildErrorLabel("Failed to load " + title + "."));
            e.printStackTrace();
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() throws IOException {
        Stage stage = (Stage) adminNameLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("School Management System");
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private Label buildErrorLabel(String text) {
        Label l = new Label("⚠ " + text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #c0392b;");
        l.setPadding(new Insets(16, 0, 0, 0));
        return l;
    }
}