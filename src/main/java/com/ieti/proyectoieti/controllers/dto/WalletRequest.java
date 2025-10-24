package com.ieti.proyectoieti.controllers.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WalletRequest {

    @NotBlank(message = "Wallet name is required")
    @Size(max = 50, message = "Wallet name must not exceed 50 characters")
    @Schema(description = "Name of the shared wallet", example = "Team Expenses", required = true)
    private String name;

    @NotBlank(message = "Creator ID is required")
    @Schema(description = "ID of the user creating the wallet", example = "user-123", required = true)
    private String creatorId;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }
}