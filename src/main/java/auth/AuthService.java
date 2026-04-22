package auth;

import models.Account;
import models.Admin;
import models.Faculty;
import models.Student;
import models.enums.UserType;

import java.io.IOException;

public class AuthService {

    public static Account login(String email, String password) throws IOException, InterruptedException {

        Account account = AuthRepository.getAccountByEmail(email);
        if (account == null)
            throw new IllegalArgumentException("No account found with that email");

        if (!account.getPassword().equals(password))
            throw new IllegalArgumentException("Incorrect password");

        return account; // only returns if everything passed
    }
}
