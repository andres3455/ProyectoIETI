package com.ieti.proyectoieti.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ieti.proyectoieti.controllers.dto.TransactionRequest;
import com.ieti.proyectoieti.controllers.dto.WalletRequest;
import com.ieti.proyectoieti.models.SharedWallet;
import com.ieti.proyectoieti.services.WalletService;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

  @Mock private WalletService walletService;

  @InjectMocks private WalletController walletController;

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
    WalletRequest walletRequest = new WalletRequest();
    walletRequest.setName(WALLET_NAME);
    walletRequest.setCreatorId(USER_ID);

    when(walletService.createWallet(WALLET_NAME, USER_ID)).thenReturn(testWallet);

    ResponseEntity<SharedWallet> response = walletController.createWallet(walletRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    assertEquals(WALLET_ID, response.getBody().getWalletId());
    verify(walletService).createWallet(WALLET_NAME, USER_ID);
  }

  @Test
  void addParticipant_ValidRequest_ReturnsWallet() {
    String newUserId = "user-789";
    Map<String, String> requestBody = Map.of("userId", newUserId);

    when(walletService.addParticipant(WALLET_ID, newUserId)).thenReturn(testWallet);

    ResponseEntity<SharedWallet> response = walletController.addParticipant(WALLET_ID, requestBody);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(walletService).addParticipant(WALLET_ID, newUserId);
  }

  @Test
  void addFunds_ValidRequest_ReturnsWallet() {
    double amount = 100.0;
    TransactionRequest transactionRequest = new TransactionRequest();
    transactionRequest.setUserId(USER_ID);
    transactionRequest.setAmount(amount);

    when(walletService.addFunds(WALLET_ID, amount, USER_ID)).thenReturn(testWallet);

    ResponseEntity<SharedWallet> response = walletController.addFunds(WALLET_ID, transactionRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(walletService).addFunds(WALLET_ID, amount, USER_ID);
  }

  @Test
  void spendFunds_ValidRequest_ReturnsWallet() {
    double amount = 50.0;
    String description = "Test expense";
    TransactionRequest transactionRequest = new TransactionRequest();
    transactionRequest.setUserId(USER_ID);
    transactionRequest.setAmount(amount);
    transactionRequest.setDescription(description);

    when(walletService.spendFunds(WALLET_ID, amount, USER_ID, description)).thenReturn(testWallet);

    ResponseEntity<SharedWallet> response = walletController.spendFunds(WALLET_ID, transactionRequest);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertNotNull(response.getBody());
    verify(walletService).spendFunds(WALLET_ID, amount, USER_ID, description);
  }

  @Test
  void getUserWallets_ValidUserId_ReturnsWallets() {
    List<SharedWallet> wallets = Arrays.asList(testWallet);
    when(walletService.getUserWallets(USER_ID)).thenReturn(wallets);

    ResponseEntity<List<SharedWallet>> response = walletController.getUserWallets(USER_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
    assertEquals(testWallet, response.getBody().get(0));
  }

  @Test
  void getWalletBalance_ValidWalletId_ReturnsBalance() {
    double balance = 150.0;
    when(walletService.getWalletBalance(WALLET_ID)).thenReturn(balance);

    ResponseEntity<Map<String, Double>> response = walletController.getWalletBalance(WALLET_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(balance, response.getBody().get("balance"));
  }

  @Test
  void getParticipantBalance_ValidParameters_ReturnsBalance() {
    double balance = 75.0;
    when(walletService.getParticipantBalance(WALLET_ID, USER_ID)).thenReturn(balance);

    ResponseEntity<Map<String, Double>> response = walletController.getParticipantBalance(WALLET_ID, USER_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(balance, response.getBody().get("balance"));
  }

  @Test
  void getAllWallets_ReturnsAllWallets() {
    List<SharedWallet> wallets = Arrays.asList(testWallet);
    when(walletService.getAllWallets()).thenReturn(wallets);

    ResponseEntity<List<SharedWallet>> response = walletController.getAllWallets();

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
    assertEquals(testWallet, response.getBody().get(0));
  }

  @Test
  void deleteWallet_ValidWalletId_ReturnsSuccessMessage() {
    doNothing().when(walletService).deleteWallet(WALLET_ID);

    ResponseEntity<Map<String, String>> response = walletController.deleteWallet(WALLET_ID);

    assertNotNull(response);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("Wallet deleted successfully", response.getBody().get("message"));
    verify(walletService).deleteWallet(WALLET_ID);
  }
}