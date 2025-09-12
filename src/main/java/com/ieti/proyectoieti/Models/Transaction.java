package com.ieti.proyectoieti.Models;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Schema(description = "Financial transaction entity representing money movement in a shared wallet")
public class Transaction {
    @Field("user_id")
    @Schema(
            description = "Unique identifier of the user who performed the transaction",
            example = "user-456",
            required = true
    )
    private String userId;

    @Field("amount")
    @Schema(
            description = "Amount of money involved in the transaction. Positive for deposits, negative for expenses",
            example = "-50.25",
            required = true
    )
    private double amount;

    @Field("description")
    @Schema(
            description = "Description or purpose of the transaction",
            example = "Lunch expenses at restaurant",
            maxLength = 200
    )
    private String description;

    @Field("date")
    @Schema(
            description = "Timestamp when the transaction was recorded",
            example = "2024-01-15T12:30:45.123Z",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private LocalDateTime date;

    public Transaction() {
        // Default constructor for MongoDB
    }

    public Transaction(String userId, double amount, String description) {
        this.userId = userId;
        this.amount = amount;
        this.description = description;
        this.date = LocalDateTime.now();
    }

    @Schema(description = "User ID who performed the transaction", required = true)
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Schema(
            description = "Transaction amount (positive for deposits, negative for expenses)",
            required = true
    )
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Schema(description = "Transaction description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Schema(description = "Transaction timestamp", accessMode = Schema.AccessMode.READ_ONLY)
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    @Schema(hidden = true)
    public boolean isDeposit() {
        return amount > 0;
    }

    @Schema(hidden = true)
    public boolean isExpense() {
        return amount < 0;
    }

    @Schema(hidden = true)
    public double getAbsoluteAmount() {
        return Math.abs(amount);
    }
}