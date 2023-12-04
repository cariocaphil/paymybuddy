package com.paymybuddy.services;

import com.paymybuddy.models.User;
import com.paymybuddy.repository.UserRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
  private final UserRepository userRepository;

  @Autowired
  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public User getUserById(long userId) {
    Optional<User> userOptional = userRepository.findById(userId);
    return userOptional.orElse(null);
  }
}
