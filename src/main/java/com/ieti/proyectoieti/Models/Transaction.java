package com.ieti.proyectoieti.Models;

import java.time.LocalDateTime;

public class Transaction {
    private String userId;
    private double amount;
    private String description;
    private LocalDateTime date;

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
