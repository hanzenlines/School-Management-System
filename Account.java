import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Account {

    private String accountId;
    private String passwordHash;
    private LocalDateTime lastLogin;
    private Map<String, Object> notifPrefs;

    private List<String> loginHistory;

    public Account(String accountId, String initialPassword) {
        this.accountId    = accountId;
        this.passwordHash = hashPassword(initialPassword);
        this.lastLogin    = null;
        this.notifPrefs   = new HashMap<>();
        this.loginHistory = new ArrayList<>();

        this.notifPrefs.put("announcements", true);
        this.notifPrefs.put("grades",        true);
        this.notifPrefs.put("enrollment",    true);
        this.notifPrefs.put("email_alerts",  false);
    }

    public void updateEmail(String newEmail) {
        if (newEmail == null || !newEmail.contains("@")) {
            System.out.println("  [ERROR] Invalid email address.");
            return;
        }
        notifPrefs.put("email", newEmail);
        System.out.println("  [OK] Email updated to: " + newEmail);
    }

    public boolean changePassword(String currentPassword, String newPassword) {
        if (!hashPassword(currentPassword).equals(this.passwordHash)) {
            System.out.println("  [ERROR] Current password is incorrect.");
            return false;
        }
        if (newPassword.length() < 8) {
            System.out.println("  [ERROR] New password must be at least 8 characters.");
            return false;
        }
        this.passwordHash = hashPassword(newPassword);
        System.out.println("  [OK] Password changed successfully.");
        return true;
    }

    public List<String> getLoginHistory() {
        if (loginHistory.isEmpty()) {
            System.out.println("  No login history found.");
        } else {
            System.out.println("  Login history for account [" + accountId + "]:");
            for (int i = 0; i < loginHistory.size(); i++) {
                System.out.println("    " + (i + 1) + ". " + loginHistory.get(i));
            }
        }
        return loginHistory;
    }

    public boolean login(String password) {
        if (hashPassword(password).equals(this.passwordHash)) {
            this.lastLogin = LocalDateTime.now();
            String entry = lastLogin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            loginHistory.add(entry);
            System.out.println("  [OK] Login successful at " + entry);
            return true;
        }
        System.out.println("  [ERROR] Incorrect password.");
        return false;
    }

    public String getAccountId()    { return accountId; }
    public LocalDateTime getLastLogin() { return lastLogin; }
    public Map<String, Object> getNotifPrefs() { return notifPrefs; }

    private String hashPassword(String plain) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plain.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found.", e);
        }
    }

    @Override
    public String toString() {
        String lastLoginStr = (lastLogin == null)
                ? "Never"
                : lastLogin.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        return  "  Account ID  : " + accountId + "\n" +
                "  Password    : [hashed] " + passwordHash.substring(0, 16) + "...\n" +
                "  Last Login  : " + lastLoginStr + "\n" +
                "  Notif Prefs : " + notifPrefs;
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Account account = null;

        printBanner();

        boolean running = true;
        while (running) {
            if (account == null) {
                printGuestMenu();
                System.out.print("  > ");
                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1" -> account = handleRegister(sc);
                    case "2" -> account = handleLogin(sc);
                    case "0" -> { running = false; System.out.println("\n  Goodbye!\n"); }
                    default  -> System.out.println("  [!] Invalid option.\n");
                }
            } else {
                printAccountMenu(account.getAccountId());
                System.out.print("  > ");
                String choice = sc.nextLine().trim();
                switch (choice) {
                    case "1" -> {
                        System.out.println("\n  ── Account Details ──────────────");
                        System.out.println(account);
                        System.out.println();
                    }
                    case "2" -> {
                        System.out.println("\n  ── Update Email ─────────────────");
                        System.out.print("  New email: ");
                        String email = sc.nextLine().trim();
                        account.updateEmail(email);
                        System.out.println();
                    }
                    case "3" -> {
                        System.out.println("\n  ── Change Password ──────────────");
                        System.out.print("  Current password: ");
                        String curr = sc.nextLine().trim();
                        System.out.print("  New password    : ");
                        String newP = sc.nextLine().trim();
                        account.changePassword(curr, newP);
                        System.out.println();
                    }
                    case "4" -> {
                        System.out.println("\n  ── Login History ────────────────");
                        account.getLoginHistory();
                        System.out.println();
                    }
                    case "5" -> {
                        System.out.println("\n  ── Notification Preferences ─────");
                        account.getNotifPrefs().forEach((k, v) ->
                                System.out.println("  " + k + " : " + v));
                        System.out.println();
                    }
                    case "0" -> {
                        account = null;
                        System.out.println("\n  Logged out.\n");
                    }
                    default -> System.out.println("  [!] Invalid option.\n");
                }
            }
        }
        sc.close();
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════╗");
        System.out.println("  ║   School Management System           ║");
        System.out.println("  ║   <<security>> Account Class Demo    ║");
        System.out.println("  ╚══════════════════════════════════════╝");
        System.out.println();
    }

    private static void printGuestMenu() {
        System.out.println("  ── Main Menu ────────────────────────");
        System.out.println("  [1] Register new account");
        System.out.println("  [2] Log in");
        System.out.println("  [0] Exit");
        System.out.println("  ─────────────────────────────────────");
    }

    private static void printAccountMenu(String id) {
        System.out.println("  ── Account Menu [" + id + "] ──");
        System.out.println("  [1] View account details");
        System.out.println("  [2] Update email");
        System.out.println("  [3] Change password");
        System.out.println("  [4] View login history");
        System.out.println("  [5] View notification preferences");
        System.out.println("  [0] Log out");
        System.out.println("  ─────────────────────────────────────");
    }

    private static Account handleRegister(Scanner sc) {
        System.out.println("\n  ── Register ─────────────────────────");
        System.out.print("  Account ID : ");
        String id = sc.nextLine().trim();
        System.out.print("  Password   : ");
        String pw = sc.nextLine().trim();
        if (id.isEmpty() || pw.isEmpty()) {
            System.out.println("  [ERROR] ID and password cannot be empty.\n");
            return null;
        }
        Account acc = new Account(id, pw);
        System.out.println("  [OK] Account created for: " + id);
        System.out.println();
        return acc;
    }

    private static Account handleLogin(Scanner sc) {
        System.out.println("\n  ── Log In ───────────────────────────");
        System.out.print("  Account ID : ");
        String id = sc.nextLine().trim();
        System.out.print("  Password   : ");
        String pw = sc.nextLine().trim();

        Account acc = new Account(id, pw);
        boolean ok = acc.login(pw);
        System.out.println();
        return ok ? acc : null;
    }
}