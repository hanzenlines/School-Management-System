package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.PaymentPlan;
import models.enums.Semester;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Balance {
    private final String id;
    private final String studentId;
    private final String schoolYear;
    private final Semester semester;
    private final PaymentPlan paymentPlan;
    private double totalCost;
    private boolean downpaymentPaid;
    private double remainingBalance;
    private boolean discountApplied;

    @JsonCreator
    public Balance(
            @JsonProperty("id")               String id,
            @JsonProperty("studentId")        String studentId,
            @JsonProperty("schoolYear")       String schoolYear,
            @JsonProperty("semester")         Semester semester,
            @JsonProperty("paymentPlan")      PaymentPlan paymentPlan,
            @JsonProperty("totalCost")        double totalCost,
            @JsonProperty("downpaymentPaid")  boolean downpaymentPaid,
            @JsonProperty("remainingBalance") double remainingBalance,
            @JsonProperty("discountApplied")  boolean discountApplied
    ) {
        this.id = id;
        this.studentId = studentId;
        this.schoolYear = schoolYear;
        this.semester = semester;
        this.paymentPlan = paymentPlan;
        this.totalCost = totalCost;
        this.downpaymentPaid = downpaymentPaid;
        this.remainingBalance = remainingBalance;
        this.discountApplied = discountApplied;
    }

    public String getId() { return id; }
    public String getStudentId() { return studentId; }
    public String getSchoolYear() { return schoolYear; }
    public Semester getSemester() { return semester; }
    public PaymentPlan getPaymentPlan() { return paymentPlan; }
    public double getTotalCost() { return totalCost; }
    public boolean isDownpaymentPaid() { return downpaymentPaid; }
    public double getRemainingBalance() { return remainingBalance; }
    public boolean isDiscountApplied() { return discountApplied; }

    public void setTotalCost(double totalCost) {
        if (totalCost < 0)
            throw new IllegalArgumentException("Total cost cannot be negative");
        this.totalCost = totalCost;
    }

    public void setDownpaymentPaid(boolean downpaymentPaid) {
        this.downpaymentPaid = downpaymentPaid;
    }

    public void setRemainingBalance(double remainingBalance) {
        if (remainingBalance < 0)
            throw new IllegalArgumentException("Remaining balance cannot be negative");
        this.remainingBalance = remainingBalance;
    }

    public void setDiscountApplied(boolean discountApplied) {
        this.discountApplied = discountApplied;
    }
}