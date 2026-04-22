import java.util.ArrayList;
import java.util.Scanner;

public class Account {

    private String accountId;
    private String password;
    private String email;
    private String lastLogin;
    private ArrayList<String> loginHistory;

    public Account(String accountId, String password) {
        this.accountId = accountId;
        this.password = password;
        this.email = "";
        this.lastLogin = "No login yet";
        this.loginHistory = new ArrayList<String>();
    }

    public void updateEmail(String newEmail) {
        if (newEmail.contains("@")) {
            this.email = newEmail;
            System.out.println("Email updated.");
        } else {
            System.out.println("Invalid email.");
        }
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (this.password.equals(oldPassword)) {
            if (newPassword.length() >= 6) {
                this.password = newPassword;
                System.out.println("Password changed.");
            } else {
                System.out.println("Password too short.");
            }
        } else {
            System.out.println("Wrong password.");
        }
    }

    public void getLoginHistory() {
        System.out.println("Login History: " + accountId);
        if (loginHistory.isEmpty()) {
            System.out.println("No history yet.");
        } else {
            for (int i = 0; i < loginHistory.size(); i++) {
                System.out.println((i + 1) + ". " + loginHistory.get(i));
            }
        }
    }

    public boolean login(String inputPassword) {
        if (this.password.equals(inputPassword)) {
            String timeNow = java.time.LocalDateTime.now()
                             .toString().replace("T", " ").substring(0, 19);
            this.lastLogin = timeNow;
            this.loginHistory.add(timeNow);
            System.out.println("Login successful.");
            return true;
        } else {
            System.out.println("Wrong password.");
            return false;
        }
    }

    public void displayInfo() {
        System.out.println("Account ID : " + accountId);
        System.out.println("Email      : " + email);
        System.out.println("Last Login : " + lastLogin);
    }

    public String getAccountId() {
        return accountId;
    }

    public String getEmail() {
        return email;
    }

    public String getLastLogin() {
        return lastLogin;
    }

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        Account myAccount = null;
        int choice;

        boolean running = true;
        while (running) {

            if (myAccount == null) {

                System.out.println("\n1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.print("Choice: ");

                choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 1) {
                    System.out.print("Account ID: ");
                    String id = scanner.nextLine();
                    System.out.print("Password: ");
                    String pass = scanner.nextLine();

                    if (id.isEmpty() || pass.isEmpty()) {
                        System.out.println("Fields cannot be empty.");
                    } else {
                        myAccount = new Account(id, pass);
                        System.out.println("Account created.");
                    }

                } else if (choice == 2) {
                    System.out.print("Account ID: ");
                    String id = scanner.nextLine();
                    System.out.print("Password: ");
                    String pass = scanner.nextLine();

                    myAccount = new Account(id, pass);
                    myAccount.login(pass);

                } else if (choice == 3) {
                    running = false;
                    System.out.println("Exiting.");
                } else {
                    System.out.println("Invalid choice.");
                }

            } else {

                System.out.println("\n1. View Info");
                System.out.println("2. Update Email");
                System.out.println("3. Change Password");
                System.out.println("4. Login History");
                System.out.println("5. Logout");
                System.out.print("Choice: ");

                choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 1) {
                    myAccount.displayInfo();

                } else if (choice == 2) {
                    System.out.print("New email: ");
                    String newEmail = scanner.nextLine();
                    myAccount.updateEmail(newEmail);

                } else if (choice == 3) {
                    System.out.print("Current password: ");
                    String oldPass = scanner.nextLine();
                    System.out.print("New password: ");
                    String newPass = scanner.nextLine();
                    myAccount.changePassword(oldPass, newPass);

                } else if (choice == 4) {
                    myAccount.getLoginHistory();

                } else if (choice == 5) {
                    myAccount = null;
                    System.out.println("Logged out.");

                } else {
                    System.out.println("Invalid choice.");
                }
            }
        }

        scanner.close();
    }
    }
