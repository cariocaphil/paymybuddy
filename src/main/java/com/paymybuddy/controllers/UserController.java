package com.paymybuddy.controllers;

import com.paymybuddy.models.User;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path="api/v1/users")
public class UserController {
  @GetMapping
  public List<User> getAllUsers(){
    List<User> users = Arrays.asList(new User(101, "friend1@example.com", "friend1_social", 50.0, new ArrayList<>()));
  return users;
  }
}
