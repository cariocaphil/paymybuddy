package com.paymybuddy.services;

import com.paymybuddy.models.Currency;
import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.User;
import com.paymybuddy.models.WithdrawRequest;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private TransactionService transactionService;

  private User sender;
  private Transaction transaction;

  @BeforeEach
  void setUp() {
    sender = new User();
    sender.setUserID(1L);
    sender.setEmail("sender@example.com");
    sender.setSocialMediaAcc(User.SocialMediaAccount.Twitter);
    sender.setBalance(500.0);
    sender.setPassword("password123");
    sender.setCurrency(Currency.USD);
  }

  // Adjusted for WithdrawRequest
  @Test
  void withdrawToBank_WithSufficientFunds_PerformsWithdrawalSuccessfully() {
    // Given
    WithdrawRequest withdrawRequest = new WithdrawRequest(sender.getUserID(), 100.0, Currency.USD);

    when(userRepository.findById(sender.getUserID())).thenReturn(Optional.of(sender));

    // When
    transactionService.withdrawToBank(withdrawRequest);

    // Then
    assertEquals(400.0, sender.getBalance(), "The balance after withdrawal is incorrect.");

    // Verify userRepository.save() was called to persist the new balance
    verify(userRepository).save(sender);

    // Verify a withdrawal transaction was recorded
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void withdrawToBank_WithInsufficientFunds_ThrowsException() {
    // Arrange
    WithdrawRequest withdrawRequest = new WithdrawRequest(sender.getUserID(), sender.getBalance() + 1.0, Currency.USD); // More than the user has

    when(userRepository.findById(sender.getUserID())).thenReturn(Optional.of(sender));

    // Act & Assert
    assertThrows(IllegalStateException.class, () -> transactionService.withdrawToBank(withdrawRequest));

    // Verify that no changes were persisted
    verify(userRepository, never()).save(any(User.class));
    verify(transactionRepository, never()).save(any(Transaction.class));
  }
}