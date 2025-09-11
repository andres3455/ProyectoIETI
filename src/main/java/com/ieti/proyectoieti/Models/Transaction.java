package com.ieti.proyectoieti.Models;

import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;

public class Transaction {
    @Field("user_id")
    private String userId;

    @Field("amount")
    private double amount;

    @Field("description")
    private String description;

    @Field("date")
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}