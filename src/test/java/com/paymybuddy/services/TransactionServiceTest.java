package com.paymybuddy.services;

import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.User;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
  private User receiver;
  private Transaction transaction;

  @BeforeEach
  void setUp() {
    sender = new User();
    sender.setUserID(1L);
    sender.setEmail("sender@example.com");
    sender.setSocialMediaAcc(User.SocialMediaAccount.Twitter);
    sender.setBalance(500.0);
    sender.setPassword("password123");

    receiver = new User();
    receiver.setUserID(2L);
    receiver.setEmail("receiver@example.com");
    receiver.setSocialMediaAcc(User.SocialMediaAccount.Facebook);
    receiver.setBalance(200.0);
    receiver.setPassword("password456");

    transaction = new Transaction();
    transaction.setAmount(50.0);
    transaction.setFee(5.0);
    transaction.setSender(sender);
    transaction.setReceiver(receiver);
  }

  @Test
  void makePayment_sufficientFunds_transfersFundsCorrectly() {
    // Arrange
    when(userRepository.findById(sender.getUserID())).thenReturn(Optional.of(sender));
    when(userRepository.findById(receiver.getUserID())).thenReturn(Optional.of(receiver));

    // Act
    transactionService.makePayment(transaction);

    // Assert
    assertEquals(445.0, sender.getBalance());
    assertEquals(250.0, receiver.getBalance());

    // Verify that save was called on both users
    verify(userRepository).save(sender);
    verify(userRepository).save(receiver);

    // Verify that save was called on the transaction
    verify(transactionRepository).save(transaction);
  }

  @Test
  void makePayment_insufficientFunds_throwsException() {
    // Arrange
    sender.setBalance(40.0); // Less than the transaction amount + fee
    when(userRepository.findById(sender.getUserID())).thenReturn(Optional.of(sender));
    when(userRepository.findById(receiver.getUserID())).thenReturn(Optional.of(receiver));

    // Act & Assert
    Exception exception = assertThrows(IllegalStateException.class, () -> {
      transactionService.makePayment(transaction);
    });

    // Assert the exception message
    assertEquals("Insufficient funds for this transaction.", exception.getMessage());

    // Verify that userRepository.save() was never called since the transaction should fail
    verify(userRepository, never()).save(any(User.class));

    // Verify that transactionRepository.save() was also never called
    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void getTransactionById_WithValidId_ReturnsTransaction() {
    // Arrange
    Long validId = transaction.getTransactionID();
    when(transactionRepository.findById(validId)).thenReturn(Optional.of(transaction));

    // Act
    Transaction foundTransaction = transactionService.getTransactionById(validId);

    // Assert
    assertEquals(transaction, foundTransaction);

    // Verify interaction
    verify(transactionRepository).findById(validId);
  }

  @Test
  void getTransactionById_WithInvalidId_ThrowsException() {
    // Arrange
    Long invalidTransactionId = 999L; // Assume this ID does not exist
    when(transactionRepository.findById(invalidTransactionId)).thenReturn(Optional.empty());

    // Act & Assert
    Exception exception = assertThrows(IllegalArgumentException.class, () -> {
      transactionService.getTransactionById(invalidTransactionId);
    });

    // Assert the exception message
    assertEquals("Transaction not found.", exception.getMessage());

    // Verify interaction
    verify(transactionRepository).findById(invalidTransactionId);
  }

  @Test
  void withdrawToBank_WithSufficientFunds_PerformsWithdrawalSuccessfully() {
    // Given
    Long userId = sender.getUserID();
    double initialBalance = sender.getBalance();
    double withdrawalAmount = 100.0;
    double expectedBalanceAfterWithdrawal = initialBalance - withdrawalAmount;
    when(userRepository.findById(userId)).thenReturn(Optional.of(sender));

    // When
    transactionService.withdrawToBank(userId, withdrawalAmount);

    // Then
    assertEquals(expectedBalanceAfterWithdrawal, sender.getBalance(), "The balance after withdrawal is incorrect.");

    // Verify userRepository.save() was called to persist the new balance
    verify(userRepository).save(sender);

    // Verify a withdrawal transaction was recorded
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void withdrawToBank_WithInsufficientFunds_ThrowsException() {
    // Arrange
    Long userId = sender.getUserID();
    double withdrawalAmount = sender.getBalance() + 1.0; // More than the user has
    when(userRepository.findById(userId)).thenReturn(Optional.of(sender));

    // Act & Assert
    assertThrows(IllegalStateException.class, () -> {
      transactionService.withdrawToBank(userId, withdrawalAmount);
    });

    // No need to assert the exception message here as it's already covered in the service logic,
    // but ensure that no state changes were persisted in case of failure
    verify(userRepository, never()).save(sender);
    verify(transactionRepository, never()).save(any(Transaction.class));
  }
}
