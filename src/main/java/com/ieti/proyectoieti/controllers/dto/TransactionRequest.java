package com.ieti.proyectoieti.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TransactionRequest {

    @NotBlank(message = "User ID is required")
    @Schema(description = "ID of the user performing the transaction", example = "user-123", required = true)
    private String userId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Transaction amount", example = "100.50", required = true)
    private Double amount;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    @Schema(description = "Transaction description", example = "Lunch expenses")
    private String description;

    // Getters and setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}