package com.paymybuddy.services;

import com.paymybuddy.controllers.UserController;
import com.paymybuddy.exceptions.UserNotFoundException;
import com.paymybuddy.exceptions.UserRegistrationException;
import com.paymybuddy.models.Currency;
import com.paymybuddy.models.User;
import com.paymybuddy.models.UserRegistrationRequest;
import com.paymybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceTest {

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private UserService userService;

  // Create a PasswordEncoder field
  @MockBean
  private PasswordEncoder passwordEncoder;

  @Mock
  private CurrencyConversionService currencyConversionService;

  @BeforeEach
  void setUp() {
    // Instantiate the PasswordEncoder
    this.passwordEncoder = new BCryptPasswordEncoder();

    this.currencyConversionService = mock(CurrencyConversionService.class);

    // Initialize the UserService with the mocked UserRepository and real PasswordEncoder
    this.userService = new UserService(userRepository, passwordEncoder, currencyConversionService);

    // Assuming a direct currency conversion without rates for simplicity in tests
    given(currencyConversionService.convertCurrency(anyDouble(), any(Currency.class), any(Currency.class)))
        .willAnswer(invocation -> invocation.getArgument(0));
  }

  @Test
  void testGetAllUsers() {
    // Mock the behavior of the userRepository.findAll() method
    User user1 = new User(1L, "user1@example.com", User.SocialMediaAccount.Twitter, 100.0);
    User user2 = new User(2L, "user2@example.com", User.SocialMediaAccount.Facebook, 150.0);
    List<User> userList = Arrays.asList(user1, user2);

    when(userRepository.findAll()).thenReturn(userList);

    // Call the method to get all users
    List<User> allUsers = userService.getAllUsers();

    // Assert the result
    assertEquals(2, allUsers.size());
    assertEquals(userList, allUsers);
  }
  @Test
  void addFriend_SuccessfullyAddsFriend() {
    // Arrange
    long userId = 1L;
    long friendId = 2L;

    User user = new User(userId, "user@example.com", User.SocialMediaAccount.Twitter, 100.0);
    User friend = new User(friendId, "friend@example.com", User.SocialMediaAccount.Facebook, 50.0);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));

    // Act
    userService.addFriend(userId, friendId);

    // Assert
    verify(userRepository, times(2)).save(any());
  }

  @Test
  void addFriend_UserNotFoundThrowsException() {
    // Arrange
    long userId = 1L;
    long friendId = 2L;

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act and Assert
    assertThrows(UserNotFoundException.class, () -> userService.addFriend(userId, friendId));
    verify(userRepository, never()).save(any());
  }


  @Test
  void testGetUserById() {
    // Arrange
    long userId = 1L;
    User expectedUser = new User(userId, "test@example.com", "Twitter", 100.0);

    // Mock the behavior of UserRepository.findById
    when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

    // Act
    User actualUser = userService.getUserById(userId);

    // Assert
    assertEquals(expectedUser, actualUser);

    // Unhappy path: Attempt to retrieve a user with an invalid/non-existent ID
    User nonExistingUser = userService.getUserById(999L);
    assertNull(nonExistingUser);
  }

  @Test
  void loadMoney_ValidUser_Success() {
    // Arrange
    long userId = 1L;
    double amount = 50.0;
    Currency currency = Currency.EUR; // Define the currency

    User user = new User(userId, "test@example.com", User.SocialMediaAccount.Twitter, 100.0);
    user.setCurrency(currency); // Set the currency for the user
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // Act
    userService.loadMoney(userId, amount, currency); // Make sure to pass the currency to loadMoney

    // Assert
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void loadMoney_InvalidUser_ThrowsUserNotFoundException() {
    // Arrange
    long userId = 1L;
    double amount = 50.0;
    Currency currency = Currency.USD;

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act/Assert
    assertThrows(UserNotFoundException.class, () -> userService.loadMoney(userId, amount, currency));

    // Assert
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  public void testRegisterUser() {
    // Arrange
    UserRegistrationRequest request = new UserRegistrationRequest();
    request.setEmail("test@example.com");
    request.setSocialMediaAcc("Twitter");
    request.setBalance(0.0);
    request.setPassword("password");
    request.setCurrency(String.valueOf(Currency.EUR));

    // Assume no existing user with the provided email
    when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    // Act
    userService.registerUser(request); // Adjusted to pass the entire request

    // Assert
    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(userCaptor.capture());
    User savedUser = userCaptor.getValue();

    // Verify the saved user details
    assertEquals(request.getEmail(), savedUser.getEmail());
    assertEquals(User.SocialMediaAccount.valueOf(request.getSocialMediaAcc()), savedUser.getSocialMediaAcc());
    assertEquals(0.0, savedUser.getBalance());
    assertTrue(passwordEncoder.matches("password", savedUser.getPassword()), "Password should be encoded and match");
  }

  @Test
  public void testRegisterUserWhenUserExists() {
    // Arrange
    String email = "test@example.com";
    String socialMediaAcc = "Twitter";
    double balance = 0.0;
    String password = "password";
    Currency currency = Currency.EUR;
    String bankAccountNumber = "123456789";
    String bankName = "Bank A";
    String bankRoutingNumber = "111000025";

    // Create a UserRegistrationRequest object
    UserRegistrationRequest request = new UserRegistrationRequest();
    request.setEmail(email);
    request.setSocialMediaAcc(socialMediaAcc);
    request.setBalance(balance);
    request.setPassword(password);
    request.setCurrency(String.valueOf(currency));
    request.setBankAccountNumber(bankAccountNumber);
    request.setBankName(bankName);
    request.setBankRoutingNumber(bankRoutingNumber);

    // Mock the userRepository behavior to simulate an existing user with the provided email
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

    // Act and Assert
    // Verify that UserRegistrationException is thrown when trying to register an existing user
    assertThrows(UserRegistrationException.class, () -> userService.registerUser(request));

    // Verify that userRepository's save method is not called
    verify(userRepository, never()).save(any(User.class));
  }
}
