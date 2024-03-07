package com.paymybuddy.services;

import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.User;
import com.paymybuddy.models.WithdrawRequest;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tinylog.Logger;

@Service
public class TransactionService {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private CurrencyConversionService currencyConversionService;

  @Transactional
  public void makePayment(Transaction transaction) {
    Logger.info("Attempting to process payment for transaction: {}", transaction);

    // Authentication validation
    String authenticatedUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
    User authenticatedUser = userRepository.findByEmail(authenticatedUserEmail)
        .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

    if (transaction.getSender().getUserID() != authenticatedUser.getUserID()) {
      throw new IllegalStateException("You can only make transactions from your own account.");
    }

    User sender = userRepository.findById(authenticatedUser.getUserID())
        .orElseThrow(() -> new IllegalArgumentException("Sender not found."));
    User receiver = userRepository.findById(transaction.getReceiver().getUserID())
        .orElseThrow(() -> new IllegalArgumentException("Receiver not found."));

    // Currency validation and conversion
    double transactionAmount = transaction.getAmount();
    if (!sender.getCurrency().equals(receiver.getCurrency())) {
      transactionAmount = currencyConversionService.convertCurrency(transaction.getAmount(), sender.getCurrency(), receiver.getCurrency());
      Logger.info("Converted transaction amount from {} {} to {} {}", transaction.getAmount(), sender.getCurrency(), transactionAmount, receiver.getCurrency());
    }

    // Calculate the fee and total deduction
    double fee = transaction.getAmount() * 0.005;
    double totalDeduction = transaction.getAmount() + fee;
    if (sender.getBalance() < totalDeduction) {
      Logger.error("Insufficient funds for transaction: {}", transaction);
      throw new IllegalStateException("Insufficient funds for this transaction.");
    }

    // Processing the transaction
    transaction.setFee(fee);
    sender.setBalance(sender.getBalance() - totalDeduction);
    receiver.setBalance(receiver.getBalance() + transactionAmount); // Use the potentially converted amount

    userRepository.save(sender);
    userRepository.save(receiver);
    transaction.setTimestamp(LocalDateTime.now());
    transactionRepository.save(transaction);
    Logger.info("Payment processed successfully for transaction: {}", transaction);
  }

  public Transaction getTransactionById(Long transactionId) {
    Logger.info("Retrieving transaction with ID: {}", transactionId);
    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));
    Logger.info("Transaction retrieved successfully: {}", transaction);
    return transaction;
  }

  @Transactional
  public void withdrawToBank(Long userId, double amount) {
    String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User authenticatedUser = userRepository.findByEmail(authenticatedUsername)
        .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

    if (!userId.equals(authenticatedUser.getUserID())) {
      Logger.error("Attempted withdrawal for user ID: {} by authenticated user ID: {}", userId, authenticatedUser.getUserID());
      throw new IllegalStateException("Withdrawals can only be made from the authenticated user's account.");
    }

    // Convert the withdrawal amount to the user's currency if necessary
    double amountToWithdraw = amount;
    if (!authenticatedUser.getCurrency().equals("USD")) { // Assuming USD is the base currency for the example
      amountToWithdraw = currencyConversionService.convertCurrency(amount, "USD", authenticatedUser.getCurrency());
      Logger.info("Converted withdrawal amount to user's currency: {} {}", amountToWithdraw, authenticatedUser.getCurrency());
    }

    if (authenticatedUser.getBalance() < amountToWithdraw) {
      Logger.error("Insufficient funds for withdrawal for user ID: {}", userId);
      throw new IllegalStateException("Insufficient funds for withdrawal.");
    }

    authenticatedUser.setBalance(authenticatedUser.getBalance() - amountToWithdraw);
    userRepository.save(authenticatedUser);

    Transaction withdrawalTransaction = new Transaction();
    withdrawalTransaction.setAmount(-amountToWithdraw); // Negative to indicate withdrawal
    withdrawalTransaction.setTimestamp(LocalDateTime.now());
    withdrawalTransaction.setDescription("Withdrawal to bank account");
    withdrawalTransaction.setFee(0); // Assuming no fee, adjust as necessary
    withdrawalTransaction.setSender(authenticatedUser); // User is both sender and receiver
    withdrawalTransaction.setReceiver(null); // No external receiver
    transactionRepository.save(withdrawalTransaction);

    Logger.info("Withdrawal to bank completed successfully for user ID: {}", userId);
  }
}
