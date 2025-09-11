package com.ieti.proyectoieti.Controllers;

import com.ieti.proyectoieti.Models.SharedWallet;
import com.ieti.proyectoieti.Services.WalletService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    public SharedWallet createWallet(@RequestBody Map<String, String> body) {
        return walletService.createWallet(body.get("name"), body.get("creatorId"));
    }

    @PostMapping("/{walletId}/participants")
    public SharedWallet addParticipant(@PathVariable String walletId,
                                       @RequestBody Map<String, String> body) {
        return walletService.addParticipant(walletId, body.get("userId"));
    }

    @PostMapping("/{walletId}/deposit")
    public SharedWallet addFunds(@PathVariable String walletId,
                                 @RequestBody Map<String, Object> body) {
        double amount = Double.parseDouble(body.get("amount").toString());
        return walletService.addFunds(walletId, amount, body.get("userId").toString());
    }

    @PostMapping("/{walletId}/spend")
    public SharedWallet spendFunds(@PathVariable String walletId,
                                   @RequestBody Map<String, Object> body) {
        double amount = Double.parseDouble(body.get("amount").toString());
        String userId = body.get("userId").toString();
        String description = body.get("description").toString();

        return walletService.spendFunds(walletId, amount, userId, description);
    }

    @GetMapping("/user/{userId}")
    public List<SharedWallet> getUserWallets(@PathVariable String userId) {
        return walletService.getUserWallets(userId);
    }

    @GetMapping("/{walletId}/balance")
    public Map<String, Double> getWalletBalance(@PathVariable String walletId) {
        double balance = walletService.getWalletBalance(walletId);
        return Map.of("balance", balance);
    }

    @GetMapping("/{walletId}/participant/{userId}/balance")
    public Map<String, Double> getParticipantBalance(@PathVariable String walletId,
                                                     @PathVariable String userId) {
        double balance = walletService.getParticipantBalance(walletId, userId);
        return Map.of("balance", balance);
    }

    @GetMapping
    public List<SharedWallet> getAllWallets() {
        return walletService.getAllWallets();
    }

    @DeleteMapping("/{walletId}")
    public Map<String, String> deleteWallet(@PathVariable String walletId) {
        walletService.deleteWallet(walletId);
        return Map.of("message", "Wallet deleted successfully");
    }
}