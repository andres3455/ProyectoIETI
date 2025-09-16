package com.ieti.proyectoieti.Controllers;

import com.ieti.proyectoieti.Models.SharedWallet;
import com.ieti.proyectoieti.Services.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
@Tag(name = "Wallets", description = "Shared wallet management APIs")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @Operation(
            summary = "Create a new shared wallet",
            description = "Creates a new shared wallet with the specified name and creator"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet created successfully",
                    content = @Content(schema = @Schema(implementation = SharedWallet.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    })
    @PostMapping
    public SharedWallet createWallet(
            @Parameter(description = "Wallet creation details", required = true)
            @RequestBody Map<String, String> body) {
        return walletService.createWallet(body.get("name"), body.get("creatorId"));
    }

    @Operation(
            summary = "Add participant to wallet",
            description = "Adds a user as a participant to an existing wallet"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participant added successfully",
                    content = @Content(schema = @Schema(implementation = SharedWallet.class))),
            @ApiResponse(responseCode = "400", description = "Wallet not found or invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @PostMapping("/{walletId}/participants")
    public SharedWallet addParticipant(
            @Parameter(description = "Wallet ID", required = true) @PathVariable String walletId,
            @Parameter(description = "User ID to add", required = true) @RequestBody Map<String, String> body) {
        return walletService.addParticipant(walletId, body.get("userId"));
    }

    @Operation(
            summary = "Add funds to wallet",
            description = "Deposits funds into the specified wallet"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funds added successfully",
                    content = @Content(schema = @Schema(implementation = SharedWallet.class))),
            @ApiResponse(responseCode = "400", description = "Invalid amount or wallet not found")
    })
    @PostMapping("/{walletId}/deposit")
    public SharedWallet addFunds(
            @Parameter(description = "Wallet ID", required = true) @PathVariable String walletId,
            @Parameter(description = "Deposit details", required = true) @RequestBody Map<String, Object> body) {
        double amount = Double.parseDouble(body.get("amount").toString());
        return walletService.addFunds(walletId, amount, body.get("userId").toString());
    }

    @Operation(
            summary = "Spend funds from wallet",
            description = "Spends funds from the specified wallet with a description"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Funds spent successfully",
                    content = @Content(schema = @Schema(implementation = SharedWallet.class))),
            @ApiResponse(responseCode = "400", description = "Insufficient funds or invalid parameters")
    })
    @PostMapping("/{walletId}/spend")
    public SharedWallet spendFunds(
            @Parameter(description = "Wallet ID", required = true) @PathVariable String walletId,
            @Parameter(description = "Spending details", required = true) @RequestBody Map<String, Object> body) {
        double amount = Double.parseDouble(body.get("amount").toString());
        String userId = body.get("userId").toString();
        String description = body.get("description").toString();

        return walletService.spendFunds(walletId, amount, userId, description);
    }

    @Operation(
            summary = "Get user's wallets",
            description = "Retrieves all wallets where the user is a participant"
    )
    @ApiResponse(responseCode = "200", description = "User wallets retrieved successfully",
            content = @Content(schema = @Schema(implementation = SharedWallet[].class)))
    @GetMapping("/user/{userId}")
    public List<SharedWallet> getUserWallets(
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        return walletService.getUserWallets(userId);
    }

    @Operation(
            summary = "Get wallet balance",
            description = "Retrieves the total balance of a wallet"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Balance retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @GetMapping("/{walletId}/balance")
    public Map<String, Double> getWalletBalance(
            @Parameter(description = "Wallet ID", required = true) @PathVariable String walletId) {
        double balance = walletService.getWalletBalance(walletId);
        return Map.of("balance", balance);
    }

    @Operation(
            summary = "Get participant balance",
            description = "Retrieves the balance of a specific participant in a wallet"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Participant balance retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Wallet or participant not found")
    })
    @GetMapping("/{walletId}/participant/{userId}/balance")
    public Map<String, Double> getParticipantBalance(
            @Parameter(description = "Wallet ID", required = true) @PathVariable String walletId,
            @Parameter(description = "User ID", required = true) @PathVariable String userId) {
        double balance = walletService.getParticipantBalance(walletId, userId);
        return Map.of("balance", balance);
    }

    @Operation(
            summary = "Get all wallets",
            description = "Retrieves all wallets in the system"
    )
    @ApiResponse(responseCode = "200", description = "All wallets retrieved successfully",
            content = @Content(schema = @Schema(implementation = SharedWallet[].class)))
    @GetMapping
    public List<SharedWallet> getAllWallets() {
        return walletService.getAllWallets();
    }

    @Operation(
            summary = "Delete wallet",
            description = "Deletes a wallet by its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    @DeleteMapping("/{walletId}")
    public Map<String, String> deleteWallet(
            @Parameter(description = "Wallet ID", required = true) @PathVariable String walletId) {
        walletService.deleteWallet(walletId);
        return Map.of("message", "Wallet deleted successfully");
    }
}