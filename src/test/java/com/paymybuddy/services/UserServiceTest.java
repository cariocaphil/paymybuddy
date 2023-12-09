package com.paymybuddy.services;

import com.paymybuddy.controllers.UserController;
import com.paymybuddy.exceptions.UserNotFoundException;
import com.paymybuddy.exceptions.UserRegistrationException;
import com.paymybuddy.models.User;
import com.paymybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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

  @Test
  void testGetAllUsers() {
    // Mock the UserRepository or use an in-memory database for testing
    UserRepository userRepositoryMock = mock(UserRepository.class);

    // Mock the behavior of the userRepository.findAll() method
    User user1 = new User(1L, "user1@example.com", "Twitter", 100.0);
    User user2 = new User(2L, "user2@example.com", "Facebook", 150.0);
    List<User> userList = Arrays.asList(user1, user2);

    when(userRepositoryMock.findAll()).thenReturn(userList);

    // Create a UserService instance with the mocked UserRepository
    UserService userService = new UserService(userRepositoryMock);

    // Create a UserController instance with the UserService
    UserController userController = new UserController(userService);

    // Call the method to get all users
    List<User> allUsers = userController.getAllUsers();

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

    User user = new User(userId, "test@example.com", "Twitter", 100.0);
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    // Act
    userService.loadMoney(userId, amount);

    // Assert
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void loadMoney_InvalidUser_ThrowsUserNotFoundException() {
    // Arrange
    long userId = 1L;
    double amount = 50.0;

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    // Act/Assert
    assertThrows(UserNotFoundException.class, () -> userService.loadMoney(userId, amount));

    // Assert
    verify(userRepository, times(1)).findById(userId);
    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  public void testRegisterUser() {
    // Arrange
    String email = "test@example.com";
    String socialMediaAcc = "Twitter";
    double balance = 0.0;
    String password = "password";

    // Mock the userRepository behavior
    when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

    // Act
    userService.registerUser(email, socialMediaAcc, balance, password);

    // Assert
    // Verify that userRepository's save method is called with the expected user
    verify(userRepository, times(1)).save(argThat(user ->
        user.getEmail().equals(email) &&
            user.getSocialMediaAcc().equals(User.SocialMediaAccount.Twitter) &&
            user.getBalance() == 0.0
    ));
  }

  @Test
  public void testRegisterUserWhenUserExists() {
    // Arrange
    String email = "test@example.com";
    String socialMediaAcc = "Twitter";
    double balance = 0.0;
    String password = "password";

    // Mock the userRepository behavior to simulate an existing user
    when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));

    // Act and Assert
    // Verify that UserRegistrationException is thrown when trying to register an existing user
    assertThrows(UserRegistrationException.class,
        () -> userService.registerUser(email, socialMediaAcc, balance, password));

    // Verify that userRepository's save method is not called
    verify(userRepository, never()).save(any(User.class));
  }
}
