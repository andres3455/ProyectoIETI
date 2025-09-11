package com.ieti.proyectoieti.Services;

import com.ieti.proyectoieti.Models.SharedWallet;
import com.ieti.proyectoieti.Repositories.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    public SharedWallet createWallet(String name, String creatorId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Wallet name cannot be empty");
        }
        if (creatorId == null || creatorId.trim().isEmpty()) {
            throw new IllegalArgumentException("Creator ID cannot be empty");
        }

        SharedWallet wallet = new SharedWallet(name);
        wallet.addParticipant(creatorId);
        return walletRepository.save(wallet);
    }

    public SharedWallet addParticipant(String walletId, String userId) {
        SharedWallet wallet = getWalletById(walletId);
        wallet.addParticipant(userId);
        return walletRepository.save(wallet);
    }

    public SharedWallet addFunds(String walletId, double amount, String userId) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        SharedWallet wallet = getWalletById(walletId);
        wallet.addFunds(amount, userId);
        return walletRepository.save(wallet);
    }

    public SharedWallet spendFunds(String walletId, double amount, String userId, String description) {
        SharedWallet wallet = getWalletById(walletId);
        wallet.spendFunds(amount, userId, description);
        return walletRepository.save(wallet);
    }

    public List<SharedWallet> getUserWallets(String userId) {
        return walletRepository.findByParticipantsContaining(userId);
    }

    public double getWalletBalance(String walletId) {
        SharedWallet wallet = getWalletById(walletId);
        return wallet.getBalance();
    }

    public double getParticipantBalance(String walletId, String userId) {
        SharedWallet wallet = getWalletById(walletId);
        return wallet.getParticipantBalance(userId);
    }

    private SharedWallet getWalletById(String walletId) {
        Optional<SharedWallet> wallet = walletRepository.findById(walletId);
        if (wallet.isEmpty()) {
            throw new IllegalArgumentException("Wallet not found with ID: " + walletId);
        }
        return wallet.get();
    }

    public List<SharedWallet> getAllWallets() {
        return walletRepository.findAll();
    }

    public void deleteWallet(String walletId) {
        if (!walletRepository.existsById(walletId)) {
            throw new IllegalArgumentException("Wallet not found with ID: " + walletId);
        }
        walletRepository.deleteById(walletId);
    }
}