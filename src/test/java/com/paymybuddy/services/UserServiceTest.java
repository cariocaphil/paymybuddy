package com.paymybuddy.services;

import com.paymybuddy.dto.UserDTO;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

    // Mock the currency conversion to return the same amount for simplicity
    given(currencyConversionService.convertCurrency(anyDouble(), any(Currency.class), any(Currency.class)))
        .willAnswer(invocation -> invocation.getArgument(0));

    // Setup the security context with a mocked Authentication object
    Authentication authentication = mock(Authentication.class);
    SecurityContext securityContext = mock(SecurityContext.class);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    // Assume the email "user@example.com" belongs to the authenticated user
    when(authentication.getName()).thenReturn("user@example.com");

    // Mock the userRepository to return a User object for findByEmail
    User authenticatedUser = new User();
    authenticatedUser.setUserID(1L); // This ID should match the LoadMoneyRequest
    authenticatedUser.setEmail("user@example.com");
    authenticatedUser.setSocialMediaAcc(User.SocialMediaAccount.Twitter);
    authenticatedUser.setBalance(50.0);
    authenticatedUser.setCurrency(Currency.USD);

    // Ensure that the userRepository.findByEmail returns the authenticatedUser for the email
    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(authenticatedUser));

    SecurityContextHolder.setContext(securityContext);
  }

  @Test
  void getAllUsers_ReturnsAllUsers() {
    User user1 = new User();
    user1.setUserID(1L);
    user1.setEmail("user1@example.com");
    user1.setSocialMediaAcc(User.SocialMediaAccount.Twitter);
    user1.setBalance(100.0);
    user1.setCurrency(Currency.USD);

    User user2 = new User();
    user2.setUserID(2L);
    user2.setEmail("user2@example.com");
    user2.setSocialMediaAcc(User.SocialMediaAccount.Facebook);
    user2.setBalance(200.0);
    user2.setCurrency(Currency.EUR);

    when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

    List<User> users = userService.getAllUsers();

    assertNotNull(users);
    assertEquals(2, users.size());
    verify(userRepository).findAll();
  }

  @Test
  void getUserById_UserExists_ReturnsUser() {
    long userId = 1L;
    User user = new User();
    user.setUserID(userId);
    user.setEmail("user@example.com");
    user.setSocialMediaAcc(User.SocialMediaAccount.Twitter);
    user.setBalance(100.0);
    user.setCurrency(Currency.USD);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    Optional<User> result = userService.getUserById(userId);

    assertTrue(result.isPresent());
    assertEquals(userId, result.get().getUserID());
    verify(userRepository).findById(userId);
  }

  @Test
  void getUserById_UserDoesNotExist_ThrowsUserNotFoundException() {
    long userId = 99L;
    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    Exception exception = assertThrows(UserNotFoundException.class, () ->
        userService.getUserById(userId).orElseThrow(() -> new UserNotFoundException("User not found"))
    );

    assertTrue(exception.getMessage().contains("User not found"));
    verify(userRepository).findById(userId);
  }

  @Test
  void addFriend_BothUsersExist_FriendshipCreated() {
    long userId = 1L;
    long friendId = 2L;

    User user = new User();
    user.setUserID(userId);
    user.setEmail("user@example.com");
    user.setSocialMediaAcc(User.SocialMediaAccount.Twitter);
    user.setBalance(100.0);
    user.setCurrency(Currency.USD);

    User friend = new User();
    friend.setUserID(friendId);
    friend.setEmail("friend@example.com");
    friend.setSocialMediaAcc(User.SocialMediaAccount.Facebook);
    friend.setBalance(200.0);
    friend.setCurrency(Currency.EUR);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(userRepository.findById(friendId)).thenReturn(Optional.of(friend));

    userService.addFriend(userId, friendId);

    verify(userRepository, times(2)).save(userArgumentCaptor.capture());
    List<User> capturedUsers = userArgumentCaptor.getAllValues();

    assertTrue(capturedUsers.get(0).getConnections().contains(friend));
    assertTrue(capturedUsers.get(1).getConnections().contains(user));
  }

//  @Test
//  void loadMoney_ValidRequest_UpdatesUserBalance() {
//    // Ensure the LoadMoneyRequest uses the authenticated user's ID
//    LoadMoneyRequest request = new LoadMoneyRequest(1L, 100.0, Currency.USD);
//
//    userService.loadMoney(request);
//
//    verify(userRepository).save(userArgumentCaptor.capture());
//    User updatedUser = userArgumentCaptor.getValue();
//
//    assertEquals(150.0, updatedUser.getBalance());
//  }

//  @Test
//  void registerUser_NewUser_SavesUser() {
//    UserRegistrationRequest registrationRequest = new UserRegistrationRequest("newuser@example.com", "password", "Twitter", 0.0, "USD");
//    when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
//
//    userService.registerUser(registrationRequest);
//
//    verify(userRepository).save(userArgumentCaptor.capture());
//    User savedUser = userArgumentCaptor.getValue();
//
//    assertEquals(registrationRequest.getEmail(), savedUser.getEmail());
//    assertTrue(passwordEncoder.matches("password", savedUser.getPassword()));
//  }

  @Test
  void registerUser_ExistingEmail_ThrowsException() {
    User existingUser = new User();
    existingUser.setEmail("existing@example.com");

    UserRegistrationRequest registrationRequest = new UserRegistrationRequest("existing@example.com", "password", "Twitter", 0.0, "USD");
    when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.of(existingUser));

    Exception exception = assertThrows(UserRegistrationException.class, () ->
        userService.registerUser(registrationRequest)
    );

    assertTrue(exception.getMessage().contains("already exists"));
  }

  @Test
  void getUserConnections_WithValidEmail_ReturnsUserDTOList() {
    // Arrange
    String email = "user@example.com";
    User user = new User(1L, "User", User.SocialMediaAccount.Twitter, 100.0, Currency.USD);
    User friend1 = new User(2L, "Friend1", User.SocialMediaAccount.Facebook, 200.0, Currency.EUR);
    User friend2 = new User(3L, "Friend2", User.SocialMediaAccount.Twitter, 300.0, Currency.USD);
    user.setConnections(Arrays.asList(friend1, friend2));

    when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

    // Act
    List<UserDTO> result = userService.getUserConnections(email);

    // Assert
    assertNotNull(result);
    assertEquals(2, result.size());
    assertTrue(result.stream().anyMatch(dto -> dto.getUserId().equals(friend1.getUserID())));
    assertTrue(result.stream().anyMatch(dto -> dto.getUserId().equals(friend2.getUserID())));
  }

  @Test
  void getUserConnections_WithInvalidEmail_ThrowsUserNotFoundException() {
    // Arrange
    String email = "nonexistent@example.com";
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // Act & Assert
    assertThrows(UserNotFoundException.class, () -> userService.getUserConnections(email));

    // Verify repository interaction
    verify(userRepository).findByEmail(email);
  }
}
