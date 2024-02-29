package com.paymybuddy.services;

import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.User;
import com.paymybuddy.models.WithdrawRequest;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tinylog.Logger;
@Service
public class TransactionService {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private UserRepository userRepository;

  @Transactional
  public void makePayment(Transaction transaction) {
    Logger.info("Attempting to process payment for transaction: {}", transaction);
    // Fetch the current state of the sender and receiver from the database
    User sender = userRepository.findById(transaction.getSender().getUserID())
        .orElseThrow(() -> new IllegalArgumentException("Sender not found."));
    User receiver = userRepository.findById(transaction.getReceiver().getUserID())
        .orElseThrow(() -> new IllegalArgumentException("Receiver not found."));

    // Check if sender's balance is sufficient
    double totalDeduction = transaction.getAmount() + transaction.getFee();
    if (sender.getBalance() < totalDeduction) {
      Logger.error("Insufficient funds for transaction: {}", transaction);
      throw new IllegalStateException("Insufficient funds for this transaction.");
    }

    // Deduct the transaction amount and fee from sender's balance
    sender.setBalance(sender.getBalance() - totalDeduction);

    // Add the transaction amount to the receiver's balance
    receiver.setBalance(receiver.getBalance() + transaction.getAmount());

    // Save both the sender and receiver to update their balances in the database
    userRepository.save(sender);
    userRepository.save(receiver);

    transaction.setTimestamp(LocalDateTime.now());

    // Finally, save the transaction
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
    Logger.info("Initiating withdrawal to bank for user ID: {} and amount: {}", userId, amount);
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));

    // Check if the user has enough balance to cover the withdrawal
    if (user.getBalance() < amount) {
      Logger.error("Insufficient funds for withdrawal for user ID: {}", userId);
      throw new IllegalStateException("Insufficient funds for withdrawal.");
    }

    // Deduct the withdrawal amount from the user's balance
    double newBalance = user.getBalance() - amount;
    user.setBalance(newBalance);
    userRepository.save(user);

    // Record the withdrawal as a transaction
    Transaction transaction = new Transaction();
    transaction.setAmount(-amount); // Negative to indicate withdrawal
    transaction.setTimestamp(LocalDateTime.now());
    transaction.setDescription("Withdrawal to bank account: " + user.getBankName());
    transaction.setFee(0); // Assuming no fee, adjust as necessary
    transaction.setSender(user); // In this context, user is both sender and receiver
    transaction.setReceiver(null); // No external receiver for a withdrawal
    transactionRepository.save(transaction);

    // Here you would typically call an external service to handle the bank transfer.
    // For example:
    // bankTransferService.transfer(user.getBankAccountNumber(), user.getBankRoutingNumber(), amount);

    // Note: The actual transfer to the bank is a complex process and would involve secure communication
    // with the bank's API or financial services provider.
    Logger.info("Withdrawal to bank completed successfully for user ID: {}", userId);
  }

}
