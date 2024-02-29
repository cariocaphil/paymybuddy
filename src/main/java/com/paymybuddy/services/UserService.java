package com.paymybuddy.services;

import com.paymybuddy.exceptions.UserNotFoundException;
import com.paymybuddy.exceptions.UserRegistrationException;
import com.paymybuddy.models.User;
import com.paymybuddy.models.User.SocialMediaAccount;
import com.paymybuddy.repository.UserRepository;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

@Service
public class UserService implements UserDetailsService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private static final TaggedLogger logger = Logger.tag("UserService");

  public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public List<User> getAllUsers() {
    List<User> users = userRepository.findAll();
    logger.info("Fetched all users");
    return users;
  }

  public void addFriend(long userId, long friendId) {
    logger.info("Attempting to add friend with id: {} to user with id: {}", friendId, userId);
    User user = getUserById(userId);
    User friend = getUserById(friendId);

    if (user == null || friend == null) {
      logger.error("User or friend not found for ids: {}, {}", userId, friendId);
      throw new UserNotFoundException("User or friend not found");
    }

    user.getConnections().add(friend);
    friend.getConnections().add(user);
    userRepository.save(user);
    userRepository.save(friend);
    logger.info("Successfully added friend relation between users with ids: {} and {}", userId, friendId);
  }

  public User getUserById(long userId) {
    Optional<User> userOptional = userRepository.findById(userId);
    return userOptional.orElse(null);
  }

  public void loadMoney(long userId, double amount) {
    logger.info("Loading {} money to user with id: {}", amount, userId);
    User user = getUserById(userId);

    if (user == null) {
      logger.error("User not found for id: {}", userId);
      throw new UserNotFoundException("User not found");
    }

    if (amount < 0) {
      logger.error("Attempt to load a negative amount: {} to user with id: {}", amount, userId);
      throw new IllegalArgumentException("Amount must be a positive or zero value");
    }

    user.setBalance(user.getBalance() + amount);
    userRepository.save(user);
    logger.info("Successfully loaded money to user with id: {}", userId);
  }

  public void registerUser(String email, String socialMediaAcc, double balance, String password) {
    logger.info("Registering user with email: {}", email);
    if (userRepository.findByEmail(email).isPresent()) {
      logger.error("User registration attempt failed, email already exists: {}", email);
      throw new UserRegistrationException("User with email " + email + " already exists");
    }

    User newUser = new User();
    newUser.setEmail(email);
    newUser.setSocialMediaAcc(SocialMediaAccount.valueOf(socialMediaAcc));
    newUser.setBalance(0.0);
    newUser.setPassword(passwordEncoder.encode(password));
    userRepository.save(newUser);
    logger.info("User registered successfully with email: {}", email);
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    logger.info("Loading user by username: {}", username);
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

    List<SimpleGrantedAuthority> authorities = Collections.emptyList();
    logger.debug("User found with username: {}, proceeding with authentication", username);
    return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
  }
}
