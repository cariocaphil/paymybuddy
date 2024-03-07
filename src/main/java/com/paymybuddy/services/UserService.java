package com.paymybuddy.services;

import com.paymybuddy.exceptions.UserNotFoundException;
import com.paymybuddy.exceptions.UserRegistrationException;
import com.paymybuddy.models.Currency;
import com.paymybuddy.models.LoadMoneyRequest;
import com.paymybuddy.models.User;
import com.paymybuddy.models.User.SocialMediaAccount;
import com.paymybuddy.models.UserRegistrationRequest;
import com.paymybuddy.repository.UserRepository;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tinylog.Logger;
import org.tinylog.TaggedLogger;

@Service
public class UserService implements UserDetailsService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final CurrencyConversionService currencyConversionService;
  private static final TaggedLogger logger = Logger.tag("UserService");

  @Autowired
  public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, CurrencyConversionService currencyConversionService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.currencyConversionService = currencyConversionService;
  }

  public List<User> getAllUsers() {
    List<User> users = userRepository.findAll();
    logger.info("Fetched all users");
    return users;
  }

  public void addFriend(long userId, long friendId) {
    User user = getUserById(userId).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    User friend = getUserById(friendId).orElseThrow(() -> new UserNotFoundException("Friend not found with ID: " + friendId));

    user.getConnections().add(friend);
    friend.getConnections().add(user);
    userRepository.save(user);
    userRepository.save(friend);
    logger.info("Successfully added friend relation between users with IDs: {} and {}", userId, friendId);
  }

  public Optional<User> getUserById(long userId) {
    return userRepository.findById(userId);
  }

  public void loadMoney(LoadMoneyRequest loadMoneyRequest) {
    String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    User authenticatedUser = userRepository.findByEmail(authenticatedUserEmail)
        .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

    // Check if the request is made by the authenticated user
    if (loadMoneyRequest.getUserId() != authenticatedUser.getUserID()) {
      throw new IllegalStateException("You can only load money into your own account.");
    }

    User user = getUserById(loadMoneyRequest.getUserId())
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + loadMoneyRequest.getUserId()));

    if (loadMoneyRequest.getAmount() < 0) {
      throw new IllegalArgumentException("Amount must be a positive value");
    }

    double adjustedAmount = loadMoneyRequest.getAmount();
    if (!user.getCurrency().equals(loadMoneyRequest.getCurrency())) {
      adjustedAmount = currencyConversionService.convertCurrency(loadMoneyRequest.getAmount(), loadMoneyRequest.getCurrency(), user.getCurrency());
      logger.info("Converted load amount from {} {} to {} {}, adjusted amount: {}", loadMoneyRequest.getAmount(), loadMoneyRequest.getCurrency(), user.getCurrency(), adjustedAmount);
    }

    user.setBalance(user.getBalance() + adjustedAmount);
    userRepository.save(user);
    logger.info("Successfully loaded {} {} to user with ID: {}. New balance: {}", adjustedAmount, user.getCurrency(), user.getUserID(), user.getBalance());
  }

  public void registerUser(UserRegistrationRequest request) {
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      throw new UserRegistrationException("User with email " + request.getEmail() + " already exists");
    }

    User newUser = new User();
    newUser.setEmail(request.getEmail());
    newUser.setSocialMediaAcc(SocialMediaAccount.valueOf(request.getSocialMediaAcc()));
    newUser.setBalance(request.getBalance());
    newUser.setCurrency(Currency.valueOf(request.getCurrency()));
    newUser.setPassword(passwordEncoder.encode(request.getPassword()));
    userRepository.save(newUser);
    logger.info("User registered successfully with email: {}", request.getEmail());
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepository.findByEmail(username)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

    List<SimpleGrantedAuthority> authorities = Collections.emptyList();
    return new org.springframework.security.core.userdetails.User(user.getEmail(), user.getPassword(), authorities);
  }
}
