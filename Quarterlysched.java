package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.enums.Quarter;
import models.enums.QuarterStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public class QuarterlySchedule {
    private final String id;
    private final String balanceId;
    private final Quarter quarter;
    private final double scheduledAmount;
    private double paidAmount;
    private double remainingAmount;
    private QuarterStatus status;

    @JsonCreator
    public QuarterlySchedule(
            @JsonProperty("id")              String id,
            @JsonProperty("balanceId")       String balanceId,
            @JsonProperty("quarter")         Quarter quarter,
            @JsonProperty("scheduledAmount") double scheduledAmount,
            @JsonProperty("paidAmount")      double paidAmount,
            @JsonProperty("remainingAmount") double remainingAmount,
            @JsonProperty("status")          QuarterStatus status
    ) {
        this.id = id;
        this.balanceId = balanceId;
        this.quarter = quarter;
        this.scheduledAmount = scheduledAmount;
        this.paidAmount = paidAmount;
        this.remainingAmount = remainingAmount;
        this.status = status;
    }

    public String getId() { return id; }
    public String getBalanceId() { return balanceId; }
    public Quarter getQuarter() { return quarter; }
    public double getScheduledAmount() { return scheduledAmount; }
    public double getPaidAmount() { return paidAmount; }
    public double getRemainingAmount() { return remainingAmount; }
    public QuarterStatus getStatus() { return status; }

    public void setPaidAmount(double paidAmount) {
        if (paidAmount < 0)
            throw new IllegalArgumentException("Paid amount cannot be negative");
        this.paidAmount = paidAmount;
    }

    public void setRemainingAmount(double remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public void setStatus(QuarterStatus status) {
        if (status == null)
            throw new IllegalArgumentException("Status cannot be null");
        this.status = status;
    }
}
