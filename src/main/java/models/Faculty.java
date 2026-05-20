package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.Account;
import models.enums.UserType;

public class Faculty extends Account {
    private final String employeeNumber;
    private String department;
    private String position;

    @JsonCreator
    public Faculty(
            @JsonProperty("id")             String id,
            @JsonProperty("name")           String name,
            @JsonProperty("email")          String email,
            @JsonProperty("password")       String password,
            @JsonProperty("contactNumber")  String contactNumber,
            @JsonProperty("employeeNumber") String employeeNumber,
            @JsonProperty("department")     String department,
            @JsonProperty("position")       String position
    ) {
        super(id, name, email, password, contactNumber, UserType.FACULTY);
        this.employeeNumber = employeeNumber;
        setDepartment(department);
        setPosition(position);
    }

    public String getEmployeeNumber() { return employeeNumber; }
    public String getDepartment() { return department; }
    public String getPosition() { return position; }

    public void setDepartment(String department) {
        if (department == null || department.isBlank())
            throw new IllegalArgumentException("Department cannot be empty");
        this.department = department;
    }

    public void setPosition(String position) {
        if (position == null || position.isBlank())
            throw new IllegalArgumentException("Position cannot be empty");
        this.position = position;
    }
}