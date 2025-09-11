package com.ieti.proyectoieti.Models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "wallets")
public class SharedWallet {
    @Id
    private String walletId;
    private String name;
    private List<String> participants;
    private double balance;
    private List<Transaction> transactions;

    public SharedWallet() {
        // Default constructor for MongoDB
    }

    public SharedWallet(String name) {
        this.walletId = java.util.UUID.randomUUID().toString();
        this.name = name;
        this.participants = new ArrayList<>();
        this.balance = 0.0;
        this.transactions = new ArrayList<>();
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public void addParticipant(String userId) {
        if (!participants.contains(userId)) {
            participants.add(userId);
        }
    }

    public void removeParticipant(String userId) {
        participants.remove(userId);
    }

    public void addFunds(double amount, String userId) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        balance += amount;
        transactions.add(new Transaction(userId, amount, "Deposit"));
    }

    public void spendFunds(double amount, String userId, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient funds in wallet");
        }
        if (!participants.contains(userId)) {
            throw new IllegalArgumentException("User is not a participant in this wallet");
        }

        balance -= amount;
        transactions.add(new Transaction(userId, -amount, description));
    }

    public double getParticipantBalance(String userId) {
        return transactions.stream()
                .filter(t -> t.getUserId().equals(userId))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}