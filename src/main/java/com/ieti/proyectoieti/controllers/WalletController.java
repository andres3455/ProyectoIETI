package com.ieti.proyectoieti.controllers;

import com.ieti.proyectoieti.controllers.dto.TransactionRequest;
import com.ieti.proyectoieti.controllers.dto.WalletRequest;
import com.ieti.proyectoieti.models.SharedWallet;
import com.ieti.proyectoieti.services.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallets")
@Tag(name = "Wallets", description = "Shared wallet management APIs")
public class WalletController {

  private final WalletService walletService;

  public WalletController(WalletService walletService) {
    this.walletService = walletService;
  }

  @Operation(summary = "Create a new shared wallet", description = "Creates a new shared wallet")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Wallet created successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid input parameters")
  })
  @PostMapping
  public ResponseEntity<SharedWallet> createWallet(@Valid @RequestBody WalletRequest walletRequest) {
    SharedWallet wallet = walletService.createWallet(walletRequest.getName(), walletRequest.getCreatorId());
    return ResponseEntity.ok(wallet);
  }

  @Operation(summary = "Add participant to wallet", description = "Adds a user as a participant to an existing wallet")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Participant added successfully"),
          @ApiResponse(responseCode = "400", description = "Wallet not found or invalid parameters"),
          @ApiResponse(responseCode = "404", description = "Wallet not found")
  })
  @PostMapping("/{walletId}/participants")
  public ResponseEntity<SharedWallet> addParticipant(
          @PathVariable String walletId,
          @Valid @RequestBody Map<String, String> body) {
    SharedWallet wallet = walletService.addParticipant(walletId, body.get("userId"));
    return ResponseEntity.ok(wallet);
  }

  @Operation(summary = "Add funds to wallet", description = "Deposits funds into the specified wallet")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Funds added successfully"),
          @ApiResponse(responseCode = "400", description = "Invalid amount or wallet not found")
  })
  @PostMapping("/{walletId}/deposit")
  public ResponseEntity<SharedWallet> addFunds(
          @PathVariable String walletId,
          @Valid @RequestBody TransactionRequest transactionRequest) {
    SharedWallet wallet = walletService.addFunds(
            walletId,
            transactionRequest.getAmount(),
            transactionRequest.getUserId());
    return ResponseEntity.ok(wallet);
  }

  @Operation(summary = "Spend funds from wallet", description = "Spends funds from the specified wallet")
  @ApiResponses({
          @ApiResponse(responseCode = "200", description = "Funds spent successfully"),
          @ApiResponse(responseCode = "400", description = "Insufficient funds or invalid parameters")
  })
  @PostMapping("/{walletId}/spend")
  public ResponseEntity<SharedWallet> spendFunds(
          @PathVariable String walletId,
          @Valid @RequestBody TransactionRequest transactionRequest) {
    SharedWallet wallet = walletService.spendFunds(
            walletId,
            transactionRequest.getAmount(),
            transactionRequest.getUserId(),
            transactionRequest.getDescription());
    return ResponseEntity.ok(wallet);
  }

  // Los demás métodos permanecen igual...
  @GetMapping("/user/{userId}")
  public ResponseEntity<List<SharedWallet>> getUserWallets(@PathVariable String userId) {
    return ResponseEntity.ok(walletService.getUserWallets(userId));
  }

  @GetMapping("/{walletId}/balance")
  public ResponseEntity<Map<String, Double>> getWalletBalance(@PathVariable String walletId) {
    double balance = walletService.getWalletBalance(walletId);
    return ResponseEntity.ok(Map.of("balance", balance));
  }

  @GetMapping("/{walletId}/participant/{userId}/balance")
  public ResponseEntity<Map<String, Double>> getParticipantBalance(
          @PathVariable String walletId, @PathVariable String userId) {
    double balance = walletService.getParticipantBalance(walletId, userId);
    return ResponseEntity.ok(Map.of("balance", balance));
  }

  @GetMapping
  public ResponseEntity<List<SharedWallet>> getAllWallets() {
    return ResponseEntity.ok(walletService.getAllWallets());
  }

  @DeleteMapping("/{walletId}")
  public ResponseEntity<Map<String, String>> deleteWallet(@PathVariable String walletId) {
    walletService.deleteWallet(walletId);
    return ResponseEntity.ok(Map.of("message", "Wallet deleted successfully"));
  }
}