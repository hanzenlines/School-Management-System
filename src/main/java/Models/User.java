package main.java.Models;

import main.java.Models.Enums.Role;

public abstract class User {
    private String userID;
    private String username;
    private String email;
    private final Role role;

    private boolean isActive;

    protected User(String userID, String username, String email, Role role, boolean isActive) {
        this.userID = userID;
        this.username = username;
        this.email = email;
        this.role = role;
        this.isActive = isActive;
    }

    public boolean login() {
        return false;
    }

    public void logout() {

    }

    public void updateProfile() {

    }
}
