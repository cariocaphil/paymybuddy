package com.paymybuddy.services;

import com.paymybuddy.exceptions.UserNotFoundException;
import com.paymybuddy.exceptions.UserRegistrationException;
import com.paymybuddy.models.Currency;
import com.paymybuddy.models.LoadMoneyRequest;
import com.paymybuddy.models.User;
import com.paymybuddy.models.UserRegistrationRequest;
import com.paymybuddy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private CurrencyConversionService currencyConversionService;

  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private UserService userService;

  @Captor
  private ArgumentCaptor<User> userArgumentCaptor;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    passwordEncoder = new BCryptPasswordEncoder();
    userService = new UserService(userRepository, passwordEncoder, currencyConversionService);
    given(currencyConversionService.convertCurrency(anyDouble(), any(Currency.class), any(Currency.class)))
        .willAnswer(invocation -> invocation.getArgument(0)); // Simplified conversion for testing
  }

  @Test
  void getAllUsers_ReturnsAllUsers() {
    User user1 = new User(1L, "email1@example.com", User.SocialMediaAccount.Twitter, 100.0, Currency.USD);
    User user2 = new User(2L, "email2@example.com", User.SocialMediaAccount.Facebook, 200.0, Currency.EUR);
    when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

    List<User> result = userService.getAllUsers();

    assertNotNull(result);
    assertEquals(2, result.size());
    verify(userRepository).findAll();
  }

  @Test
  void getUserById_UserExists_ReturnsUser() {
    long userId = 1L;
    User user = new User(userId, "email@example.com", User.SocialMediaAccount.Twitter, 100.0, Currency.USD);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    User result = userService.getUserById(userId).orElse(null);

    assertNotNull(result);
    assertEquals(userId, result.getUserID());
    verify(userRepository).findById(userId);
  }

  @Test
  void getUserById_UserDoesNotExist_ThrowsUserNotFoundException() {
    long userId = 1L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> userService.getUserById(userId).orElseThrow(() -> new UserNotFoundException("User not found")));

    verify(userRepository).findById(userId);
  }

  @Test
  void loadMoney_ValidRequest_UpdatesUserBalance() {
    long userId = 1L;
    double amount = 100.0;
    Currency currency = Currency.USD;
    LoadMoneyRequest request = new LoadMoneyRequest(userId, amount, currency);
    User user = new User(userId, "user@example.com", User.SocialMediaAccount.Twitter, 50.0, Currency.USD);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    userService.loadMoney(request);

    verify(userRepository).save(userArgumentCaptor.capture());
    User updatedUser = userArgumentCaptor.getValue();
    assertEquals(150.0, updatedUser.getBalance());
  }

  @Test
  void registerUser_NewUser_SavesUser() {
    UserRegistrationRequest request = new UserRegistrationRequest("newuser@example.com", "password", "Twitter", 0.0, "USD");
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    userService.registerUser(request);

    verify(userRepository).save(userArgumentCaptor.capture());
    User savedUser = userArgumentCaptor.getValue();
    assertEquals(request.getEmail(), savedUser.getEmail());
    assertTrue(passwordEncoder.matches(request.getPassword(), savedUser.getPassword()));
  }

  @Test
  void registerUser_ExistingEmail_ThrowsException() {
    String email = "existing@example.com";
    User existingUser = new User(1L, email, User.SocialMediaAccount.Twitter, 100.0, Currency.USD);
    UserRegistrationRequest request = new UserRegistrationRequest(email, "password", "Twitter", 0.0, "USD");
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

    assertThrows(UserRegistrationException.class, () -> userService.registerUser(request));

    verify(userRepository, never()).save(any(User.class));
  }

  // Additional tests for addFriend, and other methods as needed...

}
