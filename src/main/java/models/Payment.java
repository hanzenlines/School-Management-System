package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.PaymentMethod;
import models.enums.PaymentType;
import models.enums.Quarter;
import models.enums.Semester;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Payment {
    private final String id;
    private final String studentId;
    private final String schoolYear;
    private final Semester semester;
    private final PaymentType type;
    private final Quarter quarter;
    private final double amount;
    private final PaymentMethod paymentMethod;
    private final LocalDateTime paidAt;

    @JsonCreator
    public Payment(
            @JsonProperty("id")         String id,
            @JsonProperty("studentId")  String studentId,
            @JsonProperty("schoolYear") String schoolYear,
            @JsonProperty("semester")   Semester semester,
            @JsonProperty("type")       PaymentType type,
            @JsonProperty("quarter")    Quarter quarter,
            @JsonProperty("amount")     double amount,
            @JsonProperty("paymentMethod") PaymentMethod paymentMethod,
            @JsonProperty("paidAt")     LocalDateTime paidAt
    ) {
        this.id = id;
        this.studentId = studentId;
        this.schoolYear = schoolYear;
        this.semester = semester;
        this.type = type;
        this.quarter = quarter;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paidAt = paidAt;
    }

    public String getId() { return id; }

    public String getStudentId() { return studentId; }

    public String getSchoolYear() { return schoolYear; }

    public Semester getSemester() { return semester; }

    public PaymentType getType() { return type; }

    public Quarter getQuarter() { return quarter; }

    public double getAmount() { return amount; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }

    public LocalDateTime getPaidAt() { return paidAt; }
}
