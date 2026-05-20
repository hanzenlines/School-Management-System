package models.account;

import models.enums.UserType;

public abstract class Account {
    private final String id;
    private final UserType userType;
    private String name;
    private String email;
    private String password;
    private String contactNumber;

    public Account(String id, String name, String email, String password, String contactNumber, UserType userType) {
        this.id = id;
        this.userType = userType;
        setName(name);
        setEmail(email);
        setPassword(password);
        setContactNumber(contactNumber);
    }

    public String getId() { return id; }
//    public UserType getRole() { return userType; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getContactNumber() { return contactNumber; }
    public UserType getUserType() { return userType; }

    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Name cannot be empty");
        this.name = name;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Invalid email address");
        this.email = email;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
        this.password = password;
    }

    public void setContactNumber(String contactNumber) {
        if (contactNumber == null || !contactNumber.matches("\\d{11}"))
            throw new IllegalArgumentException("Contact number must be 11 digits");
        this.contactNumber = contactNumber;
    }
}
