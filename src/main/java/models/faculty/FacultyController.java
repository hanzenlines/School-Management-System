package models.faculty;

import features.announcements.AnnouncementService;
import features.grades.GradeService;
import features.grades.GradingPeriodRepository;
import features.rooms.RoomRepository;
import features.schedule.FacultyScheduleController;
import features.schedule.ScheduleRepository;
import features.sections.SectionDetailController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Announcement;
import models.GradingPeriod;
import models.Room;
import models.Schedule;
import models.enums.UserType;
import models.section.Section;
import models.section.SectionRepository;
import models.Subject;
import features.subject.SubjectRepository;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FacultyController {

    @FXML private Label facultyNameLabel;
    @FXML private Label pageTitleLabel;
    @FXML private VBox contentArea;
    @FXML private Button sidebarAnnouncements;
    @FXML private Button sidebarSections;
    @FXML private Button sidebarSchedule;

    private Faculty faculty;

    private static final String SIDEBAR_ACTIVE =
            "-fx-background-color: #444441; -fx-text-fill: white; -fx-font-size: 13px; " +
                    "-fx-background-radius: 6; -fx-padding: 8 12; -fx-alignment: CENTER-LEFT; -fx-cursor: hand;";

    private static final String SIDEBAR_INACTIVE =
            "-fx-background-color: transparent; -fx-text-fill: #888780; -fx-font-size: 13px; " +
                    "-fx-background-radius: 6; -fx-padding: 8 12; -fx-alignment: CENTER-LEFT; -fx-cursor: hand;";

    // ── Init ──────────────────────────────────────────────────────────────────

    public static void loadDashboard(Faculty faculty) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                FacultyController.class.getResource("/faculty_dashboard.fxml"));
        Scene scene = new Scene(loader.load());

        FacultyController controller = loader.getController();
        controller.initData(faculty);

        Stage stage = new Stage();
        stage.setTitle("Faculty Dashboard");
        stage.setScene(scene);
        stage.show();
    }

    public void initData(Faculty faculty) {
        this.faculty = faculty;
        facultyNameLabel.setText(faculty.getName());
        setSidebarActive(sidebarAnnouncements);
        loadAnnouncements();
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    private void setSidebarActive(Button active) {
        sidebarAnnouncements.setStyle(SIDEBAR_INACTIVE);
        sidebarSections.setStyle(SIDEBAR_INACTIVE);
        sidebarSchedule.setStyle(SIDEBAR_INACTIVE);
        active.setStyle(SIDEBAR_ACTIVE);
    }

    @FXML
    private void showAnnouncements() {
        if ("Announcements".equals(pageTitleLabel.getText())) return;
        setSidebarActive(sidebarAnnouncements);
        pageTitleLabel.setText("Announcements");
        contentArea.getChildren().clear();
        loadAnnouncements();
    }

    @FXML
    private void showMySections() {
        if ("My Sections".equals(pageTitleLabel.getText())) return;
        setSidebarActive(sidebarSections);
        pageTitleLabel.setText("My Sections");
        contentArea.getChildren().clear();
        loadMySections();
    }

    // ── Announcements ─────────────────────────────────────────────────────────

    private void loadAnnouncements() {
        pageTitleLabel.setText("Announcements");
        contentArea.getChildren().clear();

        new Thread(() -> {
            try {
                List<Announcement> announcements =
                        AnnouncementService.getAnnouncementsFor(UserType.FACULTY);

                Platform.runLater(() -> {
                    if (announcements.isEmpty()) {
                        contentArea.getChildren().add(
                                buildEmptyLabel("No announcements available."));
                        return;
                    }

                    DateTimeFormatter formatter =
                            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

                    for (Announcement a : announcements) {
                        VBox card = new VBox(6);
                        card.setStyle(
                                "-fx-background-color: white; -fx-border-color: #e0ded8; " +
                                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                                        "-fx-background-radius: 8; -fx-padding: 16;");

                        Label category = new Label(a.getCategory().toString()
                                + " · " + a.getPostedAt().format(formatter));
                        category.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

                        Label title = new Label(a.getTitle());
                        title.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; " +
                                "-fx-text-fill: #2c2c2a;");

                        Label content = new Label(a.getContent());
                        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f5e5a;");
                        content.setWrapText(true);

                        card.getChildren().addAll(category, title, content);
                        contentArea.getChildren().add(card);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        contentArea.getChildren().add(
                                buildErrorLabel("Failed to load announcements.")));
            }
        }).start();
    }

    // ── My Sections ───────────────────────────────────────────────────────────

    private void loadMySections() {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(buildLoadingLabel("Loading your sections..."));

        new Thread(() -> {
            try {
                List<Section> sections =
                        SectionRepository.getByFacultyId(faculty.getId());

                GradingPeriod gradingPeriod = GradingPeriodRepository.getActive();
                boolean pastDeadline = GradeService.isPastDeadline();

                Platform.runLater(() -> {
                    contentArea.getChildren().clear();

                    // deadline banner
                    if (gradingPeriod != null) {
                        String deadlineText = gradingPeriod.getDeadline()
                                .format(DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a"));
                        String bannerMsg = pastDeadline
                                ? "⚠ Grading deadline has passed (" + deadlineText
                                + "). Grades are now final."
                                : "Grading deadline: " + deadlineText;
                        String bannerStyle = pastDeadline
                                ? "-fx-font-size: 12px; -fx-text-fill: #c0392b; " +
                                "-fx-background-color: #fdecea; -fx-background-radius: 6; " +
                                "-fx-padding: 8 12;"
                                : "-fx-font-size: 12px; -fx-text-fill: #856404; " +
                                "-fx-background-color: #fff3cd; -fx-background-radius: 6; " +
                                "-fx-padding: 8 12;";
                        Label banner = new Label(bannerMsg);
                        banner.setStyle(bannerStyle);
                        banner.setWrapText(true);
                        contentArea.getChildren().add(banner);
                    }

                    if (sections.isEmpty()) {
                        contentArea.getChildren().add(
                                buildEmptyLabel("No sections assigned to you."));
                        return;
                    }

                    for (Section section : sections) {
                        contentArea.getChildren().add(
                                buildSectionCard(section, pastDeadline));
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() ->
                        contentArea.getChildren().add(
                                buildErrorLabel("Failed to load sections.")));
                e.printStackTrace();
            }
        }).start();
    }

    private VBox buildSectionCard(Section section, boolean pastDeadline) {
        VBox card = new VBox(6);
        card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-border-color: #dddcda; -fx-border-radius: 8; -fx-border-width: 1; " +
                        "-fx-cursor: hand;");
        card.setPadding(new Insets(14, 16, 14, 16));

        new Thread(() -> {
            try {
                Subject subject = SubjectRepository.getByCode(section.getSubjectCode());
                Platform.runLater(() -> {
                    HBox top = new HBox();

                    Label nameLabel = new Label(subject != null
                            ? subject.getSubjectName() : section.getSubjectCode());
                    nameLabel.setStyle(
                            "-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");
                    HBox.setHgrow(nameLabel, Priority.ALWAYS);

                    Label slotsLabel = new Label(
                            section.getCurrentCount() + "/" + section.getCapacity() + " students");
                    slotsLabel.setStyle(
                            "-fx-font-size: 12px; -fx-text-fill: #888780; " +
                                    "-fx-background-color: #f0efec; -fx-background-radius: 10; " +
                                    "-fx-padding: 2 8;");

                    top.getChildren().addAll(nameLabel, slotsLabel);

                    Label detailLabel = new Label(section.getSubjectCode()
                            + "  ·  Section " + section.getId()); // placeholder while loading

                    new Thread(() -> {
                        try {
                            List<String> scheduleIds = section.getScheduleIds();
                            StringBuilder sb = new StringBuilder();

                            for (String sid : scheduleIds) {
                                Schedule s = ScheduleRepository.getById(sid);
                                if (s == null) continue;
                                Room r = RoomRepository.getById(s.getRoomId());
                                String roomName = r != null ? r.getRoomName() : "Unknown Room";
                                if (sb.length() > 0) sb.append(" | ");
                                sb.append(s.getTimeSlot()).append(" @ ").append(roomName);
                            }

                            String display = sb.isEmpty() ? "No schedule" : sb.toString();
                            Platform.runLater(() ->
                                    detailLabel.setText(section.getSubjectCode()
                                            + "  ·  Section " + section.getId()
                                            + "  ·  " + display));
                        } catch (Exception e) {
                            // leave placeholder as-is
                        }
                    }).start();
                    detailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

                    card.getChildren().addAll(top, detailLabel);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label fallback = new Label("Section " + section.getId()
                            + " · " + section.getSubjectCode());
                    fallback.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c2c2a;");
                    card.getChildren().add(fallback);
                });
            }
        }).start();

        card.setOnMouseClicked(e -> openSectionDetail(section, pastDeadline));
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: #f8f7f5; -fx-background-radius: 8; " +
                        "-fx-border-color: #b0afac; -fx-border-radius: 8; -fx-border-width: 1; " +
                        "-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: white; -fx-background-radius: 8; " +
                        "-fx-border-color: #dddcda; -fx-border-radius: 8; -fx-border-width: 1; " +
                        "-fx-cursor: hand;"));

        return card;
    }

    private void openSectionDetail(Section section, boolean pastDeadline) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/SectionDetailView.fxml"));
            Parent root = loader.load();

            SectionDetailController controller = loader.getController();
            controller.initData(faculty, section, pastDeadline, this::showMySections);

            Stage stage = (Stage) facultyNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (IOException e) {
            contentArea.getChildren().add(
                    buildErrorLabel("Failed to open section: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // new method:
    @FXML
    private void showMySchedule() {
        if ("My Schedule".equals(pageTitleLabel.getText())) return;
        setSidebarActive(sidebarSchedule);
        pageTitleLabel.setText("My Schedule");
        contentArea.getChildren().clear();

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/FacultyScheduleView.fxml"));
            Parent view = loader.load();

            FacultyScheduleController controller = loader.getController();
            controller.initData(faculty);

            contentArea.getChildren().add(view);
        } catch (IOException e) {
            contentArea.getChildren().add(
                    buildErrorLabel("Failed to load schedule."));
        }
    }

    // ── Logout ────────────────────────────────────────────────────────────────

    @FXML
    private void handleLogout() throws IOException {
        Stage stage = (Stage) facultyNameLabel.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/login.fxml"));
        stage.setScene(new Scene(loader.load()));
        stage.setTitle("School Management System");
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private Label buildLoadingLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #888780;");
        l.setPadding(new Insets(16, 0, 0, 0));
        return l;
    }

    private Label buildEmptyLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #888780;");
        l.setPadding(new Insets(16, 0, 0, 0));
        return l;
    }

    private Label buildErrorLabel(String text) {
        Label l = new Label("⚠ " + text);
        l.setStyle("-fx-font-size: 13px; -fx-text-fill: #c0392b;");
        l.setPadding(new Insets(16, 0, 0, 0));
        return l;
    }

    public void navigateTo(String section) {
        switch (section) {
            case "sections" -> showMySections();
            default -> loadAnnouncements();
        }
    }
}