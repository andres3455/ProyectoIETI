package com.ieti.proyectoieti.Models;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "wallets")
@Schema(description = "Shared wallet entity for managing group finances and expenses")
public class SharedWallet {
    @Id
    @Schema(
            description = "Unique identifier of the wallet generated automatically",
            example = "wallet-123e4567-e89b-12d3-a456-426614174000",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private String walletId;

    @Schema(
            description = "Name of the shared wallet",
            example = "Team Expenses",
            required = true,
            maxLength = 50
    )
    private String name;

    @ArraySchema(
            arraySchema = @Schema(
                    description = "List of user IDs who are participants in this wallet",
                    example = "[\"user-456\", \"user-789\"]"
            ),
            schema = @Schema(
                    description = "User ID of a participant",
                    example = "user-456",
                    type = "string"
            )
    )
    private List<String> participants;

    @Schema(
            description = "Current total balance of the wallet in the system's currency",
            example = "150.75",
            minimum = "0.0",
            accessMode = Schema.AccessMode.READ_ONLY
    )
    private double balance;

    @ArraySchema(
            arraySchema = @Schema(
                    description = "List of all transactions performed in this wallet",
                    accessMode = Schema.AccessMode.READ_ONLY
            ),
            schema = @Schema(implementation = Transaction.class)
    )
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

    @Schema(description = "Unique wallet identifier")
    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    @Schema(description = "Wallet name", required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @ArraySchema(
            arraySchema = @Schema(description = "List of participant user IDs")
    )
    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    @Schema(description = "Current wallet balance", accessMode = Schema.AccessMode.READ_ONLY)
    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @ArraySchema(
            arraySchema = @Schema(description = "Wallet transactions", accessMode = Schema.AccessMode.READ_ONLY)
    )
    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Schema(hidden = true)
    public void addParticipant(String userId) {
        if (!participants.contains(userId)) {
            participants.add(userId);
        }
    }

    @Schema(hidden = true)
    public void removeParticipant(String userId) {
        participants.remove(userId);
    }

    @Schema(hidden = true)
    public void addFunds(double amount, String userId) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        balance += amount;
        transactions.add(new Transaction(userId, amount, "Deposit"));
    }

    @Schema(hidden = true)
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

    @Schema(hidden = true)
    public double getParticipantBalance(String userId) {
        return transactions.stream()
                .filter(t -> t.getUserId().equals(userId))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }
}