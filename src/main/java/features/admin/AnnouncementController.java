package features.admin;

import features.announcements.AnnouncementService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import models.Announcement;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class AnnouncementController {

    @FXML private VBox contentArea;

    public void initData() {
        loadAnnouncements();
    }

    private void loadAnnouncements() {
        contentArea.getChildren().clear();

        Button addBtn = new Button("+ New Announcement");
        addBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white;" +
                        "-fx-font-size: 13px; -fx-background-radius: 6;" +
                        "-fx-padding: 8 16; -fx-cursor: hand;");
        addBtn.setOnAction(e -> openAnnouncementModal(null));
        HBox toolbar = new HBox(addBtn);
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        contentArea.getChildren().add(toolbar);

        new Thread(() -> {
            try {
                List<Announcement> announcements =
                        AnnouncementService.getAllAnnouncements();

                Platform.runLater(() -> {
                    if (announcements.isEmpty()) {
                        Label empty = new Label("No announcements available.");
                        empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                        return;
                    }

                    DateTimeFormatter fmt =
                            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

                    for (Announcement a : announcements) {
                        contentArea.getChildren().add(buildCard(a, fmt));
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

    private VBox buildCard(Announcement a, DateTimeFormatter fmt) {
        VBox card = new VBox(6);
        String baseStyle =
                "-fx-background-color: white; -fx-border-color: #e0ded8; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16; -fx-cursor: hand;";
        String hoverStyle =
                "-fx-background-color: #fafaf8; -fx-border-color: #c8c6c0; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16; -fx-cursor: hand;";

        card.setStyle(baseStyle);
        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(baseStyle));
        card.setOnMouseClicked(e -> openAnnouncementModal(a));

        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label category = new Label(
                a.getCategory().toString() + " · "
                        + a.getTargetAudience().toString()
                        + " · " + a.getPostedAt().format(fmt));
        category.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

        if (a.isArchived()) {
            Label badge = new Label("ARCHIVED");
            badge.setStyle(
                    "-fx-background-color: #f0ede6; -fx-text-fill: #888780; " +
                            "-fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 2 6;");
            metaRow.getChildren().addAll(category, badge);
        } else {
            metaRow.getChildren().add(category);
        }

        Label title = new Label(a.getTitle());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        Label content = new Label(a.getContent());
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f5e5a;");
        content.setWrapText(true);

        card.getChildren().addAll(metaRow, title, content);
        return card;
    }

    private void openAnnouncementModal(Announcement announcement) {
        boolean isNew = announcement == null;

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setResizable(false);

        TextField titleField = new TextField(isNew ? "" : announcement.getTitle());
        titleField.setPromptText("Title");
        styleTextField(titleField);

        TextArea contentField = new TextArea(isNew ? "" : announcement.getContent());
        contentField.setPromptText("Content");
        contentField.setWrapText(true);
        contentField.setPrefRowCount(4);
        styleTextArea(contentField);

        ComboBox<Announcement.Category> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll(Announcement.Category.values());
        categoryBox.setValue(isNew
                ? Announcement.Category.GENERAL : announcement.getCategory());
        styleComboBox(categoryBox);

        ComboBox<Announcement.TargetAudience> audienceBox = new ComboBox<>();
        audienceBox.getItems().addAll(Announcement.TargetAudience.values());
        audienceBox.setValue(isNew
                ? Announcement.TargetAudience.ALL : announcement.getTargetAudience());
        styleComboBox(audienceBox);

        VBox form = new VBox(14);
        form.setPadding(new Insets(28));
        form.setPrefWidth(460);

        Label heading = new Label(isNew ? "New Announcement" : "Edit Announcement");
        heading.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");

        form.getChildren().addAll(
                heading,
                labeledField("Title", titleField),
                labeledField("Content", contentField),
                labeledField("Category", categoryBox),
                labeledField("Target Audience", audienceBox)
        );

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #888780; " +
                        "-fx-font-size: 13px; -fx-border-color: #e0ded8; -fx-border-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                        "-fx-padding: 7 16; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> modal.close());

        Button saveBtn = new Button(isNew ? "Publish" : "Save Changes");
        saveBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-background-radius: 6; " +
                        "-fx-padding: 7 16; -fx-cursor: hand;");

        actions.getChildren().addAll(cancelBtn, saveBtn);

        if (!isNew) {
            String archiveLabel = announcement.isArchived() ? "Unarchive" : "Archive";
            Button archiveBtn = new Button(archiveLabel);
            archiveBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #888780; " +
                            "-fx-font-size: 13px; -fx-border-color: #e0ded8; -fx-border-radius: 6; " +
                            "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                            "-fx-padding: 7 16; -fx-cursor: hand;");
            archiveBtn.setOnAction(e -> {
                if (confirmDialog(modal, archiveLabel + " Announcement",
                        "Are you sure you want to " + archiveLabel.toLowerCase()
                                + " this announcement?")) {
                    new Thread(() -> {
                        try {
                            announcement.archive();
                            AnnouncementService.archiveAnnouncement(announcement.getId());
                            Platform.runLater(() -> {
                                modal.close();
                                loadAnnouncements();
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> showError(modal,
                                    "Failed to " + archiveLabel.toLowerCase()
                                            + " announcement."));
                        }
                    }).start();
                }
            });

            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle(
                    "-fx-background-color: transparent; -fx-text-fill: #a32d2d; " +
                            "-fx-font-size: 13px; -fx-border-color: #e8c8c8; -fx-border-radius: 6; " +
                            "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                            "-fx-padding: 7 16; -fx-cursor: hand;");
            deleteBtn.setOnAction(e -> {
                if (confirmDialog(modal, "Delete Announcement",
                        "This action cannot be undone. Delete this announcement?")) {
                    new Thread(() -> {
                        try {
                            AnnouncementService.deleteAnnouncement(announcement.getId());
                            Platform.runLater(() -> {
                                modal.close();
                                loadAnnouncements();
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() ->
                                    showError(modal, "Failed to delete announcement."));
                        }
                    }).start();
                }
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            actions.getChildren().addAll(0, List.of(archiveBtn, deleteBtn, spacer));
        }

        saveBtn.setOnAction(e -> {
            String newTitle = titleField.getText().trim();
            String newContent = contentField.getText().trim();

            if (newTitle.isEmpty() || newContent.isEmpty()) {
                showError(modal, "Title and content cannot be empty.");
                return;
            }

            new Thread(() -> {
                try {
                    if (isNew) {
                        AnnouncementService.postAnnouncement(
                                newTitle, newContent,
                                categoryBox.getValue(), audienceBox.getValue());
                    } else {
                        AnnouncementService.editAnnouncement(
                                announcement.getId(), newTitle, newContent,
                                categoryBox.getValue(), audienceBox.getValue());
                    }
                    Platform.runLater(() -> {
                        modal.close();
                        loadAnnouncements();
                    });
                } catch (Exception ex) {
                    Platform.runLater(() ->
                            showError(modal, "Failed to save announcement."));
                }
            }).start();
        });

        form.getChildren().add(actions);
        modal.setScene(new Scene(form, Color.WHITE));
        modal.showAndWait();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private VBox labeledField(String labelText, Control field) {
        Label label = new Label(labelText);
        label.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #888780; -fx-font-weight: 500;");
        if (field instanceof ComboBox<?> cb) cb.setMaxWidth(Double.MAX_VALUE);
        return new VBox(5, label, field);
    }

    private void styleTextField(TextField f) {
        f.setStyle(
                "-fx-background-color: #fafaf8; -fx-border-color: #e0ded8; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-padding: 8 10; -fx-font-size: 13px;");
    }

    private void styleTextArea(TextArea f) {
        f.setStyle(
                "-fx-background-color: #fafaf8; -fx-border-color: #e0ded8; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-font-size: 13px;");
    }

    private <T> void styleComboBox(ComboBox<T> cb) {
        cb.setStyle(
                "-fx-background-color: #fafaf8; -fx-border-color: #e0ded8; " +
                        "-fx-border-radius: 6; -fx-background-radius: 6; " +
                        "-fx-border-width: 0.5; -fx-font-size: 13px;");
    }

    private boolean confirmDialog(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait()
                .filter(b -> b == ButtonType.OK)
                .isPresent();
    }

    private void showError(Stage owner, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}