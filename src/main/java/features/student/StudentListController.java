package features.student;

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
import models.Course;
import features.course.CourseRepository;
import models.Student;

import java.util.List;
import java.util.stream.Collectors;

public class StudentListController {

    @FXML private VBox contentArea;

    private List<Student> allStudents;
    private List<Course> allCourses;

    // ── Init ──────────────────────────────────────────────────────────────────

    public void initData() {
        loadStudents();
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    private void loadStudents() {
        contentArea.getChildren().clear();

        new Thread(() -> {
            try {
                allStudents = StudentRepository.getAll();
                allCourses  = CourseRepository.getAll();

                Platform.runLater(() -> {
                    // ── Toolbar: search field ─────────────────────────────────
                    TextField searchField = new TextField();
                    searchField.setPromptText("Search by name or student number...");
                    searchField.setStyle(
                            "-fx-background-color: #fafaf8; -fx-border-color: #e0ded8; " +
                            "-fx-border-radius: 6; -fx-background-radius: 6; " +
                            "-fx-border-width: 0.5; -fx-padding: 8 10; -fx-font-size: 13px;");
                    searchField.textProperty().addListener((obs, oldVal, newVal) ->
                            filterStudents(newVal.trim().toLowerCase()));

                    HBox toolbar = new HBox(searchField);
                    toolbar.setAlignment(Pos.CENTER_LEFT);
                    HBox.setHgrow(searchField, Priority.ALWAYS);
                    contentArea.getChildren().add(toolbar);

                    if (allStudents.isEmpty()) {
                        Label empty = new Label("No students found.");
                        empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
                        contentArea.getChildren().add(empty);
                        return;
                    }

                    renderStudents(allStudents);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label error = new Label("Failed to load students.");
                    error.setStyle("-fx-text-fill: #a32d2d; -fx-font-size: 13px;");
                    contentArea.getChildren().add(error);
                });
            }
        }).start();
    }

    private void filterStudents(String query) {
        if (allStudents == null) return;

        // remove old cards but keep toolbar (index 0)
        if (contentArea.getChildren().size() > 1)
            contentArea.getChildren().remove(1, contentArea.getChildren().size());

        List<Student> filtered = query.isEmpty()
                ? allStudents
                : allStudents.stream()
                        .filter(s -> s.getName().toLowerCase().contains(query)
                                || s.getStudentNumber().toLowerCase().contains(query))
                        .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            Label empty = new Label("No students match \"" + query + "\".");
            empty.setStyle("-fx-text-fill: #888780; -fx-font-size: 13px;");
            contentArea.getChildren().add(empty);
            return;
        }

        renderStudents(filtered);
    }

    private void renderStudents(List<Student> students) {
        for (Student s : students) {
            contentArea.getChildren().add(buildCard(s));
        }
    }

    // ── Card ──────────────────────────────────────────────────────────────────

    private VBox buildCard(Student student) {
        VBox card = new VBox(6);

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
        card.setOnMouseClicked(e -> openEditModal(student));

        // ── Meta row ──────────────────────────────────────────────────────────
        HBox metaRow = new HBox(8);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label meta = new Label(student.getStudentNumber()
                + "  ·  " + student.getCourse()
                + "  ·  Year " + student.getYearLevel());
        meta.setStyle("-fx-font-size: 11px; -fx-text-fill: #888780;");
        metaRow.getChildren().add(meta);

        if (student.hasPendingBalance()) {
            Label balanceBadge = new Label("BALANCE");
            balanceBadge.setStyle(
                    "-fx-background-color: #fdf0e0; -fx-text-fill: #c07000; " +
                    "-fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 2 6;");
            metaRow.getChildren().add(balanceBadge);
        }

        if (!student.isEnrollmentConfirmed()) {
            Label unconfirmedBadge = new Label("UNCONFIRMED");
            unconfirmedBadge.setStyle(
                    "-fx-background-color: #f0ede6; -fx-text-fill: #888780; " +
                    "-fx-font-size: 10px; -fx-background-radius: 4; -fx-padding: 2 6;");
            metaRow.getChildren().add(unconfirmedBadge);
        }

        Label nameLabel = new Label(student.getName());
        nameLabel.setStyle(
                "-fx-font-size: 15px; -fx-font-weight: 500; -fx-text-fill: #2c2c2a;");

        Label emailLabel = new Label(student.getEmail());
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #888780;");

        card.getChildren().addAll(metaRow, nameLabel, emailLabel);
        return card;
    }

    // ── Edit Modal ────────────────────────────────────────────────────────────

    private void openEditModal(Student student) {
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setResizable(false);

        // ── Fields ────────────────────────────────────────────────────────────
        // Course dropdown — from CourseRepository
        ComboBox<Course> courseBox = new ComboBox<>();
        courseBox.getItems().addAll(allCourses);
        courseBox.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Course c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getCode() + " — " + c.getName());
            }
        });
        courseBox.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Course c, boolean empty) {
                super.updateItem(c, empty);
                setText(empty || c == null ? null : c.getCode() + " — " + c.getName());
            }
        });
        allCourses.stream()
                .filter(c -> c.getCode().equals(student.getCourse()))
                .findFirst().ifPresent(courseBox::setValue);
        styleComboBox(courseBox);

        // Year level dropdown
        ComboBox<Integer> yearBox = new ComboBox<>();
        yearBox.getItems().addAll(1, 2, 3, 4, 5);
        yearBox.setValue(student.getYearLevel());
        styleComboBox(yearBox);

        // Flag toggles
        CheckBox enrollmentCheck = new CheckBox("Enrollment Confirmed");
        enrollmentCheck.setSelected(student.isEnrollmentConfirmed());
        enrollmentCheck.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c2c2a;");

        CheckBox balanceCheck = new CheckBox("Has Pending Balance");
        balanceCheck.setSelected(student.hasPendingBalance());
        balanceCheck.setStyle("-fx-font-size: 13px; -fx-text-fill: #2c2c2a;");

        // ── Read-only info ────────────────────────────────────────────────────
        Label studentNumberLabel = new Label(student.getStudentNumber());
        studentNumberLabel.setStyle(
                "-fx-font-size: 13px; -fx-text-fill: #2c2c2a; " +
                "-fx-background-color: #f0ede6; -fx-background-radius: 6; " +
                "-fx-padding: 8 10; -fx-border-color: #e0ded8; " +
                "-fx-border-radius: 6; -fx-border-width: 0.5;");

        // ── Form layout ───────────────────────────────────────────────────────
        VBox form = new VBox(14);
        form.setPadding(new Insets(28));
        form.setPrefWidth(440);

        Label heading = new Label(student.getName());
        heading.setStyle(
                "-fx-font-size: 18px; -fx-font-weight: 600; -fx-text-fill: #2c2c2a;");

        form.getChildren().addAll(
                heading,
                labeledField("Student Number", studentNumberLabel),
                labeledField("Course", courseBox),
                labeledField("Year Level", yearBox),
                new VBox(10, enrollmentCheck, balanceCheck)
        );

        // ── Actions ───────────────────────────────────────────────────────────
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #888780; " +
                "-fx-font-size: 13px; -fx-border-color: #e0ded8; -fx-border-radius: 6; " +
                "-fx-border-width: 0.5; -fx-background-radius: 6; " +
                "-fx-padding: 7 16; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> modal.close());

        Button saveBtn = new Button("Save Changes");
        saveBtn.setStyle(
                "-fx-background-color: #2c2c2a; -fx-text-fill: white; " +
                "-fx-font-size: 13px; -fx-background-radius: 6; " +
                "-fx-padding: 7 16; -fx-cursor: hand;");

        saveBtn.setOnAction(e -> {
            Course selectedCourse = courseBox.getValue();
            Integer selectedYear  = yearBox.getValue();

            if (selectedCourse == null || selectedYear == null) {
                showError(modal, "Please fill in all fields.");
                return;
            }

            new Thread(() -> {
                try {
                    student.setCourse(selectedCourse.getCode());
                    student.setYearLevel(selectedYear);
                    student.setEnrollmentConfirmed(enrollmentCheck.isSelected());
                    student.setHasPendingBalance(balanceCheck.isSelected());
                    StudentRepository.update(student);

                    Platform.runLater(() -> {
                        modal.close();
                        loadStudents();
                    });
                } catch (IllegalArgumentException ex) {
                    Platform.runLater(() -> showError(modal, ex.getMessage()));
                } catch (Exception ex) {
                    Platform.runLater(() -> showError(modal, "Failed to save changes."));
                }
            }).start();
        });

        actions.getChildren().addAll(cancelBtn, saveBtn);
        form.getChildren().add(actions);

        modal.setScene(new Scene(form, Color.WHITE));
        modal.showAndWait();
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private VBox labeledField(String labelText, javafx.scene.Node field) {
        Label label = new Label(labelText);
        label.setStyle(
                "-fx-font-size: 12px; -fx-text-fill: #888780; -fx-font-weight: 500;");
        if (field instanceof ComboBox<?> cb) cb.setMaxWidth(Double.MAX_VALUE);
        return new VBox(5, label, field);
    }

    private <T> void styleComboBox(ComboBox<T> cb) {
        cb.setStyle(
                "-fx-background-color: #fafaf8; -fx-border-color: #e0ded8; " +
                "-fx-border-radius: 6; -fx-background-radius: 6; " +
                "-fx-border-width: 0.5; -fx-font-size: 13px;");
        cb.setMaxWidth(Double.MAX_VALUE);
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
