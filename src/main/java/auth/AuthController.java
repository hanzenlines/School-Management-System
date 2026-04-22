package auth;

import models.Account;

import java.io.IOException;
import java.util.Scanner;

public class AuthController {
    private static final Scanner scanner = new Scanner(System.in);

    public static Account promptLogin() {
        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            Account account = AuthService.login(email, password);
            System.out.println("Welcome, " + account.getName());
            return account;
        } catch (IllegalArgumentException e) {
            System.out.println("Login failed: " + e.getMessage());
            return null;
        } catch (IOException | InterruptedException e) {
            System.out.println("Connection error");
            e.printStackTrace();
            return null;
        }
    }
}
