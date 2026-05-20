package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.UserType;

public class Admin extends Account {
    private final String employeeNumber;

    @JsonCreator
    public Admin(
            @JsonProperty("id")             String id,
            @JsonProperty("name")           String name,
            @JsonProperty("email")          String email,
            @JsonProperty("password")       String password,
            @JsonProperty("contactNumber")  String contactNumber,
            @JsonProperty("employeeNumber") String employeeNumber
    ) {
        super(id, name, email, password, contactNumber, UserType.ADMIN);
        this.employeeNumber = employeeNumber;
    }

    public String getEmployeeNumber() { return employeeNumber; }

}
 
