package com.paymybuddy.controllers;

import com.paymybuddy.exceptions.UserNotFoundException;
import com.paymybuddy.models.LoadMoneyRequest;
import com.paymybuddy.models.User;
import com.paymybuddy.services.UserService;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tinylog.Logger;

@RestController
@RequestMapping(path="api/v1/users")
@AllArgsConstructor
public class UserController {
  @Autowired
  private final UserService userService;

  @GetMapping
  public List<User> getAllUsers(){
    Logger.info("Received request to get all users");
    return userService.getAllUsers();
  }

  @GetMapping("/{userId}")
  public User getUserById(@PathVariable long userId) {
    Logger.info("Received request to get user with ID: {}", userId);
    return userService.getUserById(userId);
  }

  @PostMapping("/add-friend")
  public ResponseEntity<String> addFriend(@RequestBody Map<String, Long> request) {
    Long userId = request.get("userId");
    Long friendId = request.get("friendId");
    Logger.info("Received request to add friend with ID: {} to user with ID: {}", friendId, userId);
    try {
      userService.addFriend(userId, friendId);
      Logger.info("Friend with ID: {} added to user with ID: {} successfully", friendId, userId);
      return ResponseEntity.ok("Friend added successfully");
    } catch (UserNotFoundException e) {
      Logger.error("User or friend not found for IDs: {} and {}", userId, friendId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or friend not found");
    } catch (IllegalArgumentException e) {
      Logger.error("Invalid user or friend ID for IDs: {} and {}", userId, friendId, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user or friend ID");
    } catch (Exception e) {
      Logger.error("Error adding friend for user ID: {} and friend ID: {}", userId, friendId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding friend");
    }
  }
  @PostMapping("/load-money")
  public ResponseEntity<String> loadMoney(@RequestBody LoadMoneyRequest loadMoneyRequest) {
    long userId = loadMoneyRequest.getUserId();
    double amount = loadMoneyRequest.getAmount();
    Logger.info("Received request to load money for user ID: {} with amount: {}", userId, amount);
    try {
      userService.loadMoney(userId, amount);
      Logger.info("Money loaded successfully for user ID: {} with amount: {}", userId, amount);
      return ResponseEntity.ok("Money loaded successfully");
    } catch (UserNotFoundException e) {
      Logger.error("User not found for ID: {}", userId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    } catch (IllegalArgumentException e) {
      Logger.error("Invalid user ID or amount for user ID: {} and amount: {}", userId, amount, e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID or amount");
    } catch (Exception e) {
      Logger.error("Error loading money for user ID: {} and amount: {}", userId, amount, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error loading money");
    }
  }

}
