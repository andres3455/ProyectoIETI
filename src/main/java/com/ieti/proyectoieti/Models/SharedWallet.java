package com.ieti.proyectoieti.Models;

import java.util.ArrayList;
import java.util.List;

public class SharedWallet {
    private String walletId;
    private String name;
    private List<String> participants;
    private double balance;
    private List<Transaction> transactions;

    public SharedWallet(String walletId, String name) {
        this.walletId = walletId;
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
        participants.add(userId);
    }

    public void addFunds(double amount, String userId) {
        balance += amount;
        transactions.add(new Transaction(userId, amount, "Deposit"));
    }

    public void spendFunds(double amount, String userId, String description) {
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient funds in wallet");
        }
        balance -= amount;
        transactions.add(new Transaction(userId, -amount, description));
    }

}
