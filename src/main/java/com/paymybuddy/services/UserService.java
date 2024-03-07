package com.paymybuddy.services;

import com.paymybuddy.exceptions.UserNotFoundException;
import com.paymybuddy.exceptions.UserRegistrationException;
import com.paymybuddy.models.Currency;
import com.paymybuddy.models.User;
import com.paymybuddy.models.User.SocialMediaAccount;
import com.paymybuddy.models.UserRegistrationRequest;
import com.paymybuddy.repository.UserRepository;
import java.util.Optional;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
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
  private final CurrencyConversionService currencyConversionService;

  private static final TaggedLogger logger = Logger.tag("UserService");

  public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, CurrencyConversionService currencyConversionService)
 {
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
    // Fetch the email of the currently authenticated user
    String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

    // Retrieve the authenticated User entity based on the email
    User authenticatedUser = userRepository.findByEmail(authenticatedEmail)
        .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

    // Check if the userId passed to the method matches the ID of the authenticated user
    if (authenticatedUser.getUserID() != userId) {
      logger.error("Attempt to add friend for a different user: Authenticated user ID: {}, Requested user ID: {}", authenticatedUser.getUserID(), userId);
      throw new IllegalStateException("Users can only add friends to their own account.");
    }

    // Proceed to add the friend if the check passes
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    User friend = userRepository.findById(friendId)
        .orElseThrow(() -> new UserNotFoundException("Friend not found with ID: " + friendId));

    // Adding each other as friends
    user.getConnections().add(friend);
    friend.getConnections().add(user);

    // Saving the updated entities
    userRepository.save(user);
    userRepository.save(friend);

    logger.info("Successfully added friend relation between users with IDs: {} and {}", userId, friendId);
  }

  public User getUserById(long userId) {
    Optional<User> userOptional = userRepository.findById(userId);
    return userOptional.orElse(null);
  }

  public void loadMoney(long userId, double amount, Currency currency) {
    logger.info("Loading {} money to user with id: {}", amount, userId);
    String authenticatedEmail = SecurityContextHolder.getContext().getAuthentication().getName();

    // Retrieve the authenticated User entity based on the email
    User authenticatedUser = userRepository.findByEmail(authenticatedEmail)
        .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

    // Check if the userId passed to the method matches the ID of the authenticated user
    if (authenticatedUser.getUserID() != userId) {
      logger.error("Attempt to load money for a different user: Authenticated user ID: {}, Requested user ID: {}", authenticatedUser.getUserID(), userId);
      throw new IllegalStateException("Users can only load money into their own account.");
    }

    // Proceed to load money if the check passes
    if (amount < 0) {
      logger.error("Attempt to load a negative amount: {} to user with id: {}", amount, userId);
      throw new IllegalArgumentException("Amount must be a positive or zero value");
    }

    // Ensure user has a currency set, default to USD if not
    Currency userCurrency = user.getCurrency() != null ? user.getCurrency() : Currency.USD;
    double adjustedAmount = amount;

    if (!userCurrency.equals(currency)) {
      adjustedAmount = currencyConversionService.convertCurrency(amount, currency, userCurrency);
    }

    authenticatedUser.setBalance(authenticatedUser.getBalance() + adjustedAmount);
    userRepository.save(authenticatedUser);
    logger.info("Successfully loaded {} to user with id: {}. New balance: {}", amount, userId, authenticatedUser.getBalance());
  }

  public void registerUser(UserRegistrationRequest request) {
    logger.info("Registering user with email: {}", request.getEmail());
    if (userRepository.findByEmail(request.getEmail()).isPresent()) {
      logger.error("User registration attempt failed, email already exists: {}", request.getEmail());
      throw new UserRegistrationException("User with email " + request.getEmail() + " already exists");
    }

    User newUser = new User();
    newUser.setEmail(request.getEmail());
    newUser.setSocialMediaAcc(SocialMediaAccount.valueOf(request.getSocialMediaAcc()));
    newUser.setBalance(request.getBalance());
    newUser.setCurrency(Currency.valueOf(request.getCurrency())); // Assuming you've already added a currency field
    newUser.setPassword(passwordEncoder.encode(request.getPassword()));
    // Set bank account details
    newUser.setBankAccountNumber(request.getBankAccountNumber());
    newUser.setBankName(request.getBankName());
    newUser.setBankRoutingNumber(request.getBankRoutingNumber());

    userRepository.save(newUser);
    logger.info("User registered successfully with email: {}", request.getEmail());
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
