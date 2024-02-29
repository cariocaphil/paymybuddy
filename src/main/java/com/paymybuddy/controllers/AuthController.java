package com.paymybuddy.controllers;

import com.paymybuddy.models.UserRegistrationRequest;
import com.paymybuddy.repository.UserRepository;
import com.paymybuddy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.tinylog.Logger;

@Controller
public class AuthController {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @PostMapping("/register")
  public ResponseEntity<String> registerUser(@RequestBody UserRegistrationRequest request) {
    Logger.info("Attempting to register new user with email: {}", request.getEmail());
    try {
      // Hash the user's password using the PasswordEncoder
      String hashedPassword = passwordEncoder.encode(request.getPassword());

      userService.registerUser(request.getEmail(), request.getSocialMediaAcc(),
          request.getBalance(), hashedPassword);

      Logger.info("User registered successfully with email: {}", request.getEmail());
      return ResponseEntity.ok("User registered successfully");
    } catch (Exception e) {
      Logger.error("Error during user registration for email: {}", request.getEmail(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error during user registration");
    }
  }

  // Still, we provide login success
  @GetMapping("/login-success")
  public String loginSuccess() {
    Logger.info("User logged in successfully");
    return "login-success";
  }
}
