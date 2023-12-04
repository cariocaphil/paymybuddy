package com.paymybuddy.controllers;

import com.paymybuddy.models.User;
import com.paymybuddy.services.UserService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
}
