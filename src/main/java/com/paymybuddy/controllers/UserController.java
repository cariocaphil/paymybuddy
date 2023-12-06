package com.paymybuddy.controllers;

import com.paymybuddy.exceptions.UserNotFoundException;
import com.paymybuddy.models.LoadMoneyRequest;
import com.paymybuddy.models.User;
import com.paymybuddy.models.UserRegistrationRequest;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="api/v1/users")
@AllArgsConstructor
public class UserController {
  @Autowired
  private final UserService userService;

  @GetMapping
  public List<User> getAllUsers(){
    return userService.getAllUsers();
  }

  @GetMapping("/{userId}")
  public User getUserById(@PathVariable long userId) {
    return userService.getUserById(userId);
  }

  @PostMapping("/add-friend")
  public ResponseEntity<String> addFriend(@RequestBody Map<String, Long> request) {
    try {
      Long userId = request.get("userId");
      Long friendId = request.get("friendId");
      userService.addFriend(userId, friendId);
      return ResponseEntity.ok("Friend added successfully");
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or friend not found");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user or friend ID");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding friend");
    }
  }

  @PostMapping("/load-money")
  public ResponseEntity<String> loadMoney(@RequestBody LoadMoneyRequest loadMoneyRequest) {
    try {
      long userId = loadMoneyRequest.getUserId();
      double amount = loadMoneyRequest.getAmount();

      userService.loadMoney(userId, amount);

      return ResponseEntity.ok("Money loaded successfully");
    } catch (UserNotFoundException e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID or amount");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error loading money");
    }
  }

  @PostMapping("/registration")
  public ResponseEntity<String> registerUser(@RequestBody UserRegistrationRequest request) {
    try {
      userService.registerUser(request.getEmail(), request.getSocialMediaAcc(), request.getBalance());

      return ResponseEntity.ok("User registered successfully");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error during user registration");
    }
  }

}
