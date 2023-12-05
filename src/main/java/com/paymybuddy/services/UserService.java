package com.paymybuddy.services;

import com.paymybuddy.exceptions.UserNotFoundException;
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

  public void addFriend(long userId, long friendId) {
    User user = getUserById(userId);
    User friend = getUserById(friendId);

    if (user == null || friend == null) {
      throw new UserNotFoundException("User or friend not found");
    }

    user.getConnections().add(friend);
    friend.getConnections().add(user);

    userRepository.save(user);
    userRepository.save(friend);
  }

  public User getUserById(long userId) {
    Optional<User> userOptional = userRepository.findById(userId);
    return userOptional.orElse(null);
  }

  public void loadMoney(long userId, double amount) {
    User user = getUserById(userId);

    if (user == null) {
      throw new UserNotFoundException("User not found");
    }

    // Validate the amount (positive or zero)
    if (amount < 0) {
      throw new IllegalArgumentException("Amount must be a positive or zero value");
    }

    // Update the user's balance
    user.setBalance(user.getBalance() + amount);

    // Save the updated user entity
    userRepository.save(user);
  }
}
