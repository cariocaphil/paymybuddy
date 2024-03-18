package com.paymybuddy.controllers;

import com.paymybuddy.dto.UserDTO;
import com.paymybuddy.exceptions.UserNotFoundException;
import com.paymybuddy.models.LoadMoneyRequest;
import com.paymybuddy.models.User;
import com.paymybuddy.services.UserService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tinylog.Logger;

@RestController
@RequestMapping(path = "api/v1/users")
public class UserController {

  private final UserService userService;

  @Autowired
  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public List<User> getAllUsers() {
    Logger.info("Received request to get all users");
    return userService.getAllUsers();
  }

  @GetMapping("/{userId}")
  public ResponseEntity<User> getUserById(@PathVariable long userId) {
    Logger.info("Received request to get user with ID: {}", userId);
    try {
      User user = userService.getUserById(userId).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
      return ResponseEntity.ok(user);
    } catch (UserNotFoundException e) {
      Logger.error("User not found for ID: {}", userId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
  }

  @PostMapping("/add-friend")
  public ResponseEntity<String> addFriend(@RequestBody Map<String, Long> request) {
    Long userId = request.get("userId");
    Long friendId = request.get("friendId");
    Logger.info("Received request to add friend with ID: {} to user with ID: {}", friendId, userId);
    try {
      userService.addFriend(userId, friendId);
      return ResponseEntity.ok("Friend added successfully");
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or friend not found");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding friend");
    }
  }

  @PostMapping("/load-money")
  public ResponseEntity<String> loadMoney(@RequestBody LoadMoneyRequest loadMoneyRequest) {
    Logger.info("Received request to load money: {}", loadMoneyRequest);
    try {
      userService.loadMoney(loadMoneyRequest);
      return ResponseEntity.ok("Money loaded successfully");
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error loading money");
    }
  }

  @GetMapping("/my-connections")
  public ResponseEntity<List<UserDTO>> getMyConnections() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    try {
      List<UserDTO> connections = userService.getUserConnections(email);
      return ResponseEntity.ok(connections);
    } catch (UserNotFoundException e) {
      Logger.error("User not found", e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    } catch (Exception e) {
      Logger.error("Error retrieving connections", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }
}
