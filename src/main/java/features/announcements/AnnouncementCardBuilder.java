package features.announcements;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Announcement;

import java.time.format.DateTimeFormatter;

public class AnnouncementCardBuilder {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    private AnnouncementCardBuilder() {}

    /** Read-only card — used by Faculty, Student, etc. */
    public static VBox buildCard(Announcement a) {
        VBox card = new VBox(6);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #e0ded8;" +
                        "-fx-border-width: 0.5;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 16;");

        Label category = new Label(
                a.getCategory().toString() + " · " + a.getPostedAt().format(FMT));
        category.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

        Label title = new Label(a.getTitle());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        Label content = new Label(a.getContent());
        content.setStyle("-fx-font-size: 13px; -fx-text-fill: #5f5e5a;");
        content.setWrapText(true);

        card.getChildren().addAll(category, title, content);
        return card;
    }

    /** Clickable card with hover + archived badge — used by Admin. */
    public static VBox buildClickableCard(Announcement a, Runnable onClick) {
        VBox card = buildCard(a); // reuse base

        // Archived badge
        if (a.isArchived()) {
            HBox metaRow = new HBox(8);
            metaRow.setAlignment(Pos.CENTER_LEFT);

            Label category = new Label(
                    a.getCategory().toString() + " · " +
                            a.getTargetAudience().toString() + " · " +
                            a.getPostedAt().format(FMT));
            category.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

            Label badge = new Label("ARCHIVED");
            badge.setStyle(
                    "-fx-background-color: #f0ede6;" +
                            "-fx-text-fill: #888780;" +
                            "-fx-font-size: 10px;" +
                            "-fx-background-radius: 4;" +
                            "-fx-padding: 2 6;");

            metaRow.getChildren().addAll(category, badge);
            card.getChildren().set(0, metaRow); // replace plain category label
        }

        // Hover
        String base = card.getStyle();
        String hover = base.replace("white", "#fafaf8")
                .replace("#e0ded8", "#c8c6c0") +
                "-fx-cursor: hand;";

        card.setStyle(base + "-fx-cursor: hand;");
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e -> card.setStyle(base + "-fx-cursor: hand;"));
        card.setOnMouseClicked(e -> onClick.run());

        return card;
    }
}