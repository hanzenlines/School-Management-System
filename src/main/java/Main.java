import features.auth.AuthController;
import models.Account;
import models.admin.Admin;
import models.admin.AdminController;
import models.faculty.Faculty;
import models.faculty.FacultyController;
import models.student.Student;
import models.student.StudentController;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        Account account = null;
        while (account == null) {
            account = AuthController.promptLogin();
        }

        switch (account.getUserType()) {
            case STUDENT -> StudentController.showDashboard((Student) account);
            case FACULTY -> FacultyController.showDashboard((Faculty) account);
            case ADMIN   -> AdminController.showDashboard((Admin) account);
        }
    }
}