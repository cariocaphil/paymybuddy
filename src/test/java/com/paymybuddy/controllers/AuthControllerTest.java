package com.paymybuddy.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.paymybuddy.exceptions.UserRegistrationException;
import com.paymybuddy.models.UserRegistrationRequest;
import com.paymybuddy.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

  @Mock
  private UserService userService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private AuthController authController;

  private UserRegistrationRequest userRegistrationRequest;

  @BeforeEach
  public void setUp() {
    userRegistrationRequest = new UserRegistrationRequest();
    userRegistrationRequest.setEmail("test@example.com");
    userRegistrationRequest.setBalance(0.0);
    userRegistrationRequest.setPassword("password123");
  }

  @Test
  public void testRegisterUserSuccess() {
    Mockito.lenient().when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

    ResponseEntity<String> response = authController.registerUser(userRegistrationRequest);

    assertEquals("User registered successfully", response.getBody());
  }

  @Test
  public void testRegisterUserFailure() {
    // Arrange
    UserRegistrationRequest request = new UserRegistrationRequest();
    request.setEmail("existing@example.com");
    request.setPassword("password123");

    // Simulate userService throwing UserRegistrationException for an existing user
    doThrow(new UserRegistrationException("User with email " + request.getEmail() + " already exists"))
        .when(userService).registerUser(any(UserRegistrationRequest.class));

    // Act
    ResponseEntity<String> responseEntity = authController.registerUser(request);

    // Assert
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    assertEquals("Error during user registration", responseEntity.getBody());
  }

}
