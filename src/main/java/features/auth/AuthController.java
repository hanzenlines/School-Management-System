package features.auth;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.account.Account;
import models.admin.Admin;
import models.faculty.Faculty;
import models.student.Student;
import models.student.StudentController;
import models.faculty.FacultyController;
import models.admin.AdminController;

public class AuthController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleLogin() {
        try {
            Account account = AuthService.login(
                    emailField.getText().trim(),
                    passwordField.getText()
            );

            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.close();

            switch (account.getUserType()) {
                case STUDENT -> StudentController.loadDashboard((Student) account);
                case FACULTY -> FacultyController.loadDashboard((Faculty) account);
                case ADMIN   -> AdminController.loadDashboard((Admin) account);
            }
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (Exception e) {
            errorLabel.setText("Connection error.");
        }
    }
}