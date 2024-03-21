package com.paymybuddy.services;

import com.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.models.Currency;
import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.User;
import com.paymybuddy.models.WithdrawRequest;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @Mock
  private TransactionRepository transactionRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CurrencyConversionService currencyConversionService;

  @InjectMocks
  private TransactionService transactionService;

  private User authenticatedUser;

  @BeforeEach
  void setUp() {
    authenticatedUser = new User();
    authenticatedUser.setUserID(1L);
    authenticatedUser.setEmail("user@example.com");
    authenticatedUser.setSocialMediaAcc(User.SocialMediaAccount.Twitter);
    authenticatedUser.setBalance(500.0);
    authenticatedUser.setPassword("password123");
    authenticatedUser.setCurrency(Currency.USD);

    // Mocking the security context
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    lenient().when(authentication.getName()).thenReturn(authenticatedUser.getEmail());
    SecurityContextHolder.setContext(securityContext);

    // Ensuring findByEmail returns the authenticated user
    lenient().when(userRepository.findByEmail(authenticatedUser.getEmail())).thenReturn(Optional.of(authenticatedUser));
  }

  @Test
  void withdrawToBank_WithSufficientFunds_PerformsWithdrawalSuccessfully() {
    // Given
    WithdrawRequest withdrawRequest = new WithdrawRequest(authenticatedUser.getUserID(), 100.0, Currency.USD);

    // Mocking the userRepository to simulate the balance update
    doAnswer(invocation -> {
      User user = invocation.getArgument(0);
      // This assumes the balance has been correctly updated in the service method
      authenticatedUser.setBalance(user.getBalance());
      return null;
    }).when(userRepository).save(any(User.class));

    // When
    transactionService.withdrawToBank(withdrawRequest);

    // Then
    double expectedBalance = 400.0; // Assuming an initial balance of 500.0 and a withdrawal of 100.0
    assertEquals(expectedBalance, authenticatedUser.getBalance(), "The balance after withdrawal is incorrect.");

    // Verify userRepository.save() was called to persist the new balance
    verify(userRepository).save(authenticatedUser);

    // Verify a withdrawal transaction was recorded
    verify(transactionRepository).save(any(Transaction.class));
  }

  @Test
  void withdrawToBank_WithInsufficientFunds_ThrowsException() {
    // Arrange
    WithdrawRequest withdrawRequest = new WithdrawRequest(authenticatedUser.getUserID(), authenticatedUser.getBalance() + 1.0, Currency.USD); // More than the user has

    // Act & Assert
    assertThrows(IllegalStateException.class, () -> transactionService.withdrawToBank(withdrawRequest), "Expected exception for insufficient funds not thrown.");

    // Verify that no changes were persisted
    verify(userRepository, never()).save(any(User.class));
    verify(transactionRepository, never()).save(any(Transaction.class));
  }

  @Test
  void getTransactionsForUserPageable_WhenFound_ReturnsPage() {
    // Arrange
    Long userId = 1L;
    Pageable pageable = PageRequest.of(0, 5, Sort.by("timestamp").descending());
    User user = new User();
    user.setUserID(userId);

    Transaction transaction1 = new Transaction(); // Set properties
    Transaction transaction2 = new Transaction(); // Set properties
    List<Transaction> transactions = List.of(transaction1, transaction2);
    Page<Transaction> transactionPage = new PageImpl<>(transactions, pageable, transactions.size());

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(transactionRepository.findBySenderOrReceiver(user, pageable)).thenReturn(transactionPage);

    // Act
    Page<TransactionDTO> resultPage = transactionService.getTransactionsForUserPageable(userId, pageable);

    // Assert
    assertEquals(2, resultPage.getContent().size(), "The page should contain the transactions");
    assertTrue(resultPage.getContent().stream().allMatch(dto -> dto instanceof TransactionDTO), "The content should be instances of TransactionDTO");

    // Ensure correct methods were called on the repositories
    verify(userRepository).findById(userId);
    verify(transactionRepository).findBySenderOrReceiver(user, pageable);
  }
}