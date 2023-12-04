package com.paymybuddy.services;

import com.paymybuddy.controllers.UserController;
import com.paymybuddy.models.User;
import com.paymybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
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
}
