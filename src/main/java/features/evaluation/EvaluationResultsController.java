package features.evaluation;

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
import models.FacultyEvaluation;
import models.faculty.Faculty;
import models.faculty.FacultyRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EvaluationResultsController {

    @FXML private VBox contentArea;

    private static final String[][] CRITERIA = {
            {"teachingEffectiveness", "Teaching Effectiveness"},
            {"subjectKnowledge",      "Subject Knowledge"},
            {"communication",         "Communication"},
            {"professionalism",       "Professionalism"},
            {"studentEngagement",     "Student Engagement"}
    };

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData() {
        loadResults();
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private void loadResults() {
        contentArea.getChildren().clear();

        new Thread(() -> {
            try {
                List<Faculty> allFaculty = FacultyRepository.getAll();
                List<FacultyEvaluation> allEvals =
                        EvaluationRepository.getAll();

                // group evaluations by facultyId
                Map<String, List<FacultyEvaluation>> byFaculty = allEvals.stream()
                        .collect(Collectors.groupingBy(
                                FacultyEvaluation::getFacultyId));

                Platform.runLater(() -> {
                    if (allFaculty.isEmpty()) {
                        Label empty = new Label("No faculty found.");
                        empty.setStyle(
                                "-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                        return;
                    }

                    for (Faculty faculty : allFaculty) {
                        List<FacultyEvaluation> evals =
                                byFaculty.getOrDefault(
                                        faculty.getId(), List.of());
                        contentArea.getChildren().add(
                                buildFacultyResultCard(faculty, evals));
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load evaluation results.");
                    error.setStyle(
                            "-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(error);
                });
            }
        }).start();
    }

    // ── Faculty result card ───────────────────────────────────────────────────

    private VBox buildFacultyResultCard(Faculty faculty,
                                        List<FacultyEvaluation> evals) {
        VBox card = new VBox(8);
        String base =
                "-fx-background-color: white; -fx-border-color: #e0ded8; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16; -fx-cursor: hand;";
        String hover =
                "-fx-background-color: #fafaf8; -fx-border-color: #c8c6c0; " +
                        "-fx-border-width: 0.5; -fx-border-radius: 8; " +
                        "-fx-background-radius: 8; -fx-padding: 16; -fx-cursor: hand;";

        card.setStyle(base);
        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e -> card.setStyle(base));
        card.setOnMouseClicked(e -> openDetailModal(faculty, evals));

        // ── Header row ────────────────────────────────────────────────────────
        HBox headerRow = new HBox(8);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(faculty.getName());
        nameLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label responseCount = new Label(evals.size() + " response"
                + (evals.size() != 1 ? "s" : ""));
        responseCount.setStyle(
                "-fx-font-size: 11px; -fx-text-fill: #888780; " +
                        "-fx-background-color: #f0ede6; -fx-background-radius: 4; " +
                        "-fx-padding: 2 6;");

        headerRow.getChildren().addAll(nameLabel, spacer, responseCount);

        Label deptLabel = new Label(
                faculty.getDepartment() + "  ·  " + faculty.getPosition());
        deptLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");

        card.getChildren().addAll(headerRow, deptLabel);

        if (evals.isEmpty()) {
            Label noEvals = new Label("No evaluations submitted yet.");
            noEvals.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");
            card.getChildren().add(noEvals);
            return card;
        }

        // ── Overall average ───────────────────────────────────────────────────
        double overall = evals.stream()
                .mapToDouble(FacultyEvaluation::getAverageScore)
                .average().orElse(0);

        Label overallLabel = new Label(
                "Overall Average: " + String.format("%.2f", overall) + " / 5.00");
        overallLabel.setStyle(
                "-fx-font-size: 13px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        card.getChildren().add(overallLabel);

        // ── Per-criterion averages ────────────────────────────────────────────
        for (String[] criterion : CRITERIA) {
            String key   = criterion[0];
            String label = criterion[1];

            double avg = evals.stream()
                    .mapToInt(ev -> getCriterionScore(ev, key))
                    .average().orElse(0);

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label criterionLabel = new Label(label);
            criterionLabel.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #5f5e5a;");
            criterionLabel.setPrefWidth(180);

            // simple bar visualization
            double pct = avg / 5.0;
            HBox bar = buildBar(pct);

            Label scoreLabel = new Label(String.format("%.2f", avg));
            scoreLabel.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #888780;");

            row.getChildren().addAll(criterionLabel, bar, scoreLabel);
            card.getChildren().add(row);
        }

        return card;
    }

    // ── Detail modal (comments) ───────────────────────────────────────────────

    private void openDetailModal(Faculty faculty,
                                 List<FacultyEvaluation> evals) {
        if (evals.isEmpty()) return;

        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setResizable(false);

        VBox form = new VBox(16);
        form.setPadding(new Insets(28));
        form.setPrefWidth(500);

        Label heading = new Label(faculty.getName() + " — Evaluation Results");
        heading.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");

        Label subheading = new Label(evals.size() + " response"
                + (evals.size() != 1 ? "s" : ""));
        subheading.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        form.getChildren().addAll(heading, subheading);

        // ── Per-criterion breakdown ───────────────────────────────────────────
        Label breakdownHeading = new Label("Score Breakdown");
        breakdownHeading.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #888780; -fx-font-weight: 500;");
        form.getChildren().add(breakdownHeading);

        for (String[] criterion : CRITERIA) {
            String key   = criterion[0];
            String label = criterion[1];

            double avg = evals.stream()
                    .mapToInt(ev -> getCriterionScore(ev, key))
                    .average().orElse(0);

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);

            Label criterionLabel = new Label(label);
            criterionLabel.setStyle(
                    "-fx-font-size: 13px; -fx-text-fill: #2c2c2a;");
            criterionLabel.setPrefWidth(200);

            Label scoreLabel = new Label(String.format("%.2f / 5.00", avg));
            scoreLabel.setStyle(
                    "-fx-font-size: 13px; -fx-text-fill: #888780;");

            row.getChildren().addAll(criterionLabel, scoreLabel);
            form.getChildren().add(row);
        }

        double overall = evals.stream()
                .mapToDouble(FacultyEvaluation::getAverageScore)
                .average().orElse(0);

        Label overallLabel = new Label(
                "Overall Average: " + String.format("%.2f / 5.00", overall));
        overallLabel.setStyle(
                "-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");
        form.getChildren().add(overallLabel);

        // ── Comments section ──────────────────────────────────────────────────
        List<String> comments = evals.stream()
                .map(FacultyEvaluation::getComments)
                .filter(c -> c != null && !c.isBlank())
                .collect(Collectors.toList());

        if (!comments.isEmpty()) {
            Separator sep = new Separator();
            Label commentsHeading = new Label("Student Comments");
            commentsHeading.setStyle(
                    "-fx-font-size: 12px; -fx-text-fill: #888780; " +
                            "-fx-font-weight: 500;");
            form.getChildren().addAll(sep, commentsHeading);

            for (String comment : comments) {
                Label commentLabel = new Label("\"" + comment + "\"");
                commentLabel.setWrapText(true);
                commentLabel.setStyle(
                        "-fx-font-size: 12px; -fx-text-fill: #5f5e5a; " +
                                "-fx-background-color: #fafaf8; -fx-background-radius: 6; " +
                                "-fx-padding: 8 10; -fx-border-color: #e0ded8; " +
                                "-fx-border-radius: 6; -fx-border-width: 0.5;");
                form.getChildren().add(commentLabel);
            }
        }

        // ── Close button ──────────────────────────────────────────────────────
        Button closeBtn = new Button("Close");
        closeBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white; " +
                        "-fx-font-size: 13px; -fx-background-radius: 6; " +
                        "-fx-padding: 7 16; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> modal.close());

        HBox actions = new HBox(closeBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        form.getChildren().add(actions);

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: white; -fx-background: white;");

        modal.setScene(new Scene(scroll, 520, 600));
        modal.showAndWait();
    }

    // ── Bar visualization ─────────────────────────────────────────────────────

    private HBox buildBar(double pct) {
        HBox barBg = new HBox();
        barBg.setPrefWidth(160);
        barBg.setPrefHeight(8);
        barBg.setStyle(
                "-fx-background-color: #f0ede6; " +
                        "-fx-background-radius: 4;");

        HBox fill = new HBox();
        fill.setPrefWidth(160 * pct);
        fill.setPrefHeight(8);
        fill.setStyle(
                "-fx-background-color: #2c2c2a; " +
                        "-fx-background-radius: 4;");

        barBg.getChildren().add(fill);

        HBox wrapper = new HBox(barBg);
        wrapper.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(wrapper, Priority.ALWAYS);
        return wrapper;
    }

    // ── Helper: get criterion score by key ────────────────────────────────────

    private int getCriterionScore(FacultyEvaluation eval, String key) {
        return switch (key) {
            case "teachingEffectiveness" -> eval.getTeachingEffectiveness();
            case "subjectKnowledge"      -> eval.getSubjectKnowledge();
            case "communication"         -> eval.getCommunication();
            case "professionalism"       -> eval.getProfessionalism();
            case "studentEngagement"     -> eval.getStudentEngagement();
            default -> 0;
        };
    }
}