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

    when(userRepository.findById(anyLong())).thenAnswer(invocation -> {
      Long id = invocation.getArgument(0);
      if (id.equals(sender.getUserID())) return Optional.of(sender);
      else if (id.equals(receiver.getUserID())) return Optional.of(receiver);
      return Optional.empty();
    });
  }

  @Test
  void makePayment_sufficientFunds_transfersFundsCorrectly() {
    // Execute the method under test
    transactionService.makePayment(transaction);

    // Assertions and verifications
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
    // Adjust sender's balance to be insufficient for the transaction and fee
    sender.setBalance(40.0); // Less than the transaction amount + fee

    when(userRepository.findById(sender.getUserID())).thenReturn(Optional.of(sender));
    when(userRepository.findById(receiver.getUserID())).thenReturn(Optional.of(receiver));

    // Attempt to make the payment and verify that the expected exception is thrown
    Exception exception = assertThrows(IllegalStateException.class, () -> {
      transactionService.makePayment(transaction);
    });

    // Verify the exception message
    assertEquals("Insufficient funds for this transaction.", exception.getMessage());

    // Verify that userRepository.save() was never called since the transaction should fail
    verify(userRepository, never()).save(any(User.class));

    // Verify that transactionRepository.save() was also never called
    verify(transactionRepository, never()).save(any(Transaction.class));
  }

}
