package com.ieti.proyectoieti.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ieti.proyectoieti.models.SharedWallet;
import com.ieti.proyectoieti.repositories.WalletRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

  @Mock private WalletRepository walletRepository;

  @InjectMocks private WalletService walletService;

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
  void createWallet_ValidParameters_ReturnsWallet() {
    when(walletRepository.save(any(SharedWallet.class))).thenReturn(testWallet);

    SharedWallet result = walletService.createWallet(WALLET_NAME, USER_ID);

    assertNotNull(result);
    assertEquals(WALLET_NAME, result.getName());
    assertTrue(result.getParticipants().contains(USER_ID));
    verify(walletRepository).save(any(SharedWallet.class));
  }

  @Test
  void createWallet_EmptyName_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> walletService.createWallet("", USER_ID));

    assertThrows(IllegalArgumentException.class, () -> walletService.createWallet(null, USER_ID));
  }

  @Test
  void createWallet_EmptyCreatorId_ThrowsException() {
    assertThrows(IllegalArgumentException.class, () -> walletService.createWallet(WALLET_NAME, ""));

    assertThrows(
        IllegalArgumentException.class, () -> walletService.createWallet(WALLET_NAME, null));
  }

  @Test
  void addParticipant_ValidUser_AddsParticipant() {
    String newUserId = "user-789";
    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(testWallet));
    when(walletRepository.save(any(SharedWallet.class))).thenReturn(testWallet);

    SharedWallet result = walletService.addParticipant(WALLET_ID, newUserId);

    assertTrue(result.getParticipants().contains(newUserId));
    verify(walletRepository).save(testWallet);
  }

  @Test
  void addParticipant_WalletNotFound_ThrowsException() {
    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.empty());

    assertThrows(
        IllegalArgumentException.class, () -> walletService.addParticipant(WALLET_ID, "new-user"));
  }

  @Test
  void addFunds_ValidAmount_AddsFunds() {
    double amount = 100.0;
    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(testWallet));
    when(walletRepository.save(any(SharedWallet.class))).thenReturn(testWallet);

    SharedWallet result = walletService.addFunds(WALLET_ID, amount, USER_ID);

    assertEquals(amount, result.getBalance());
    verify(walletRepository).save(testWallet);
  }

  @Test
  void addFunds_InvalidAmount_ThrowsException() {
    assertThrows(
        IllegalArgumentException.class, () -> walletService.addFunds(WALLET_ID, -50.0, USER_ID));

    assertThrows(
        IllegalArgumentException.class, () -> walletService.addFunds(WALLET_ID, 0.0, USER_ID));

    verify(walletRepository, never()).findById(anyString());
    verify(walletRepository, never()).save(any(SharedWallet.class));
  }

  @Test
  void spendFunds_ValidAmount_SpendsFunds() {
    testWallet.addFunds(200.0, USER_ID);
    double spendAmount = 100.0;
    String description = "Test expense";

    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(testWallet));
    when(walletRepository.save(any(SharedWallet.class))).thenReturn(testWallet);

    SharedWallet result = walletService.spendFunds(WALLET_ID, spendAmount, USER_ID, description);

    assertEquals(100.0, result.getBalance());
    verify(walletRepository).save(testWallet);
  }

  @Test
  void spendFunds_InsufficientFunds_ThrowsException() {
    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(testWallet));

    assertThrows(
        IllegalArgumentException.class,
        () -> walletService.spendFunds(WALLET_ID, 100.0, USER_ID, "Test"));
  }

  @Test
  void spendFunds_NonParticipant_ThrowsException() {
    testWallet.addFunds(100.0, USER_ID);
    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(testWallet));

    assertThrows(
        IllegalArgumentException.class,
        () -> walletService.spendFunds(WALLET_ID, 50.0, "non-participant", "Test"));
  }

  @Test
  void getUserWallets_ValidUser_ReturnsWallets() {
    List<SharedWallet> expectedWallets = Arrays.asList(testWallet);
    when(walletRepository.findByParticipantsContaining(USER_ID)).thenReturn(expectedWallets);

    List<SharedWallet> result = walletService.getUserWallets(USER_ID);

    assertEquals(1, result.size());
    assertEquals(testWallet, result.get(0));
  }

  @Test
  void getWalletBalance_ValidWallet_ReturnsBalance() {
    testWallet.addFunds(150.0, USER_ID);
    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(testWallet));

    double balance = walletService.getWalletBalance(WALLET_ID);

    assertEquals(150.0, balance);
  }

  @Test
  void getParticipantBalance_ValidParticipant_ReturnsBalance() {
    testWallet.addFunds(100.0, USER_ID);
    testWallet.spendFunds(50.0, USER_ID, "Test");
    when(walletRepository.findById(WALLET_ID)).thenReturn(Optional.of(testWallet));

    double balance = walletService.getParticipantBalance(WALLET_ID, USER_ID);

    assertEquals(50.0, balance);
  }

  @Test
  void deleteWallet_ExistingWallet_DeletesWallet() {
    when(walletRepository.existsById(WALLET_ID)).thenReturn(true);
    doNothing().when(walletRepository).deleteById(WALLET_ID);

    assertDoesNotThrow(() -> walletService.deleteWallet(WALLET_ID));
    verify(walletRepository).deleteById(WALLET_ID);
  }

  @Test
  void deleteWallet_NonExistingWallet_ThrowsException() {
    when(walletRepository.existsById(WALLET_ID)).thenReturn(false);

    assertThrows(IllegalArgumentException.class, () -> walletService.deleteWallet(WALLET_ID));
  }
}
