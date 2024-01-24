package com.paymybuddy.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.paymybuddy.models.UserRegistrationRequest;
import com.paymybuddy.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

    ResponseEntity<String> response = authController.registerUser(userRegistrationRequest);

    assertEquals("User registered successfully", response.getBody());
  }

  @Test
  public void testRegisterUserFailure() {
    doThrow(new RuntimeException("Test Exception")).when(passwordEncoder).encode(anyString());

    ResponseEntity<String> response = authController.registerUser(userRegistrationRequest);

    assertEquals("Error during user registration", response.getBody());
  }

  @Test
  public void testLoginSuccess() {
    String result = authController.loginSuccess();
    assertEquals("login-success", result);
  }

}
