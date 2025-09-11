package com.ieti.proyectoieti.Controllers;

import com.ieti.proyectoieti.Models.SharedWallet;
import com.ieti.proyectoieti.Services.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    private SharedWallet testWallet;
    private final String WALLET_ID = "wallet-123";
    private final String USER_ID = "user-456";
    private final String WALLET_NAME = "Test Wallet";

    @BeforeEach
    void setUp() {
        testWallet = new SharedWallet(WALLET_NAME);
        testWallet.setWalletId(WALLET_ID);
        testWallet.addParticipant(USER_ID);
    }

    @Test
    void createWallet_ValidRequest_ReturnsWallet() {
        Map<String, String> requestBody = Map.of(
                "name", WALLET_NAME,
                "creatorId", USER_ID
        );

        when(walletService.createWallet(WALLET_NAME, USER_ID)).thenReturn(testWallet);

        SharedWallet result = walletController.createWallet(requestBody);

        assertNotNull(result);
        assertEquals(WALLET_ID, result.getWalletId());
        verify(walletService).createWallet(WALLET_NAME, USER_ID);
    }

    @Test
    void addParticipant_ValidRequest_ReturnsWallet() {
        String newUserId = "user-789";
        Map<String, String> requestBody = Map.of("userId", newUserId);

        when(walletService.addParticipant(WALLET_ID, newUserId)).thenReturn(testWallet);

        SharedWallet result = walletController.addParticipant(WALLET_ID, requestBody);

        assertNotNull(result);
        verify(walletService).addParticipant(WALLET_ID, newUserId);
    }

    @Test
    void addFunds_ValidRequest_ReturnsWallet() {
        double amount = 100.0;
        Map<String, Object> requestBody = Map.of(
                "amount", amount,
                "userId", USER_ID
        );

        when(walletService.addFunds(WALLET_ID, amount, USER_ID)).thenReturn(testWallet);

        SharedWallet result = walletController.addFunds(WALLET_ID, requestBody);

        assertNotNull(result);
        verify(walletService).addFunds(WALLET_ID, amount, USER_ID);
    }

    @Test
    void spendFunds_ValidRequest_ReturnsWallet() {
        double amount = 50.0;
        String description = "Test expense";
        Map<String, Object> requestBody = Map.of(
                "amount", amount,
                "userId", USER_ID,
                "description", description
        );

        when(walletService.spendFunds(WALLET_ID, amount, USER_ID, description)).thenReturn(testWallet);

        SharedWallet result = walletController.spendFunds(WALLET_ID, requestBody);

        assertNotNull(result);
        verify(walletService).spendFunds(WALLET_ID, amount, USER_ID, description);
    }

    @Test
    void getUserWallets_ValidUserId_ReturnsWallets() {
        List<SharedWallet> wallets = Arrays.asList(testWallet);
        when(walletService.getUserWallets(USER_ID)).thenReturn(wallets);

        List<SharedWallet> result = walletController.getUserWallets(USER_ID);

        assertEquals(1, result.size());
        assertEquals(testWallet, result.get(0));
    }

    @Test
    void getWalletBalance_ValidWalletId_ReturnsBalance() {
        double balance = 150.0;
        when(walletService.getWalletBalance(WALLET_ID)).thenReturn(balance);

        Map<String, Double> result = walletController.getWalletBalance(WALLET_ID);

        assertEquals(balance, result.get("balance"));
    }

    @Test
    void getParticipantBalance_ValidParameters_ReturnsBalance() {
        double balance = 75.0;
        when(walletService.getParticipantBalance(WALLET_ID, USER_ID)).thenReturn(balance);

        Map<String, Double> result = walletController.getParticipantBalance(WALLET_ID, USER_ID);

        assertEquals(balance, result.get("balance"));
    }

    @Test
    void getAllWallets_ReturnsAllWallets() {
        List<SharedWallet> wallets = Arrays.asList(testWallet);
        when(walletService.getAllWallets()).thenReturn(wallets);

        List<SharedWallet> result = walletController.getAllWallets();

        assertEquals(1, result.size());
        assertEquals(testWallet, result.get(0));
    }

    @Test
    void deleteWallet_ValidWalletId_ReturnsSuccessMessage() {
        doNothing().when(walletService).deleteWallet(WALLET_ID);

        Map<String, String> result = walletController.deleteWallet(WALLET_ID);

        assertEquals("Wallet deleted successfully", result.get("message"));
        verify(walletService).deleteWallet(WALLET_ID);
    }
}