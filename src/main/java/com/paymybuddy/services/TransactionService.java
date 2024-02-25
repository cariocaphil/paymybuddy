package com.paymybuddy.services;

import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.User;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

  @Autowired
  private TransactionRepository transactionRepository;

  @Autowired
  private UserRepository userRepository;

  @Transactional
  public void makePayment(Transaction transaction) {
    // Fetch the current state of the sender and receiver from the database
    User sender = userRepository.findById(transaction.getSender().getUserID())
        .orElseThrow(() -> new IllegalArgumentException("Sender not found."));
    User receiver = userRepository.findById(transaction.getReceiver().getUserID())
        .orElseThrow(() -> new IllegalArgumentException("Receiver not found."));

    // Check if sender's balance is sufficient
    double totalDeduction = transaction.getAmount() + transaction.getFee();
    if (sender.getBalance() < totalDeduction) {
      throw new IllegalStateException("Insufficient funds for this transaction.");
    }

    // Deduct the transaction amount and fee from sender's balance
    sender.setBalance(sender.getBalance() - totalDeduction);

    // Add the transaction amount to the receiver's balance
    receiver.setBalance(receiver.getBalance() + transaction.getAmount());

    // Save both the sender and receiver to update their balances in the database
    userRepository.save(sender);
    userRepository.save(receiver);

    // Finally, save the transaction
    transactionRepository.save(transaction);
  }
}
