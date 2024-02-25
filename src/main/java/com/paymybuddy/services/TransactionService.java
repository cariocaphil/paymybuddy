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
    User sender = transaction.getSender();

    // Check if sender's balance is sufficient
    double totalDeduction = transaction.getAmount() + transaction.getFee();
    if (sender.getBalance() < totalDeduction) {
      throw new IllegalStateException("Insufficient funds for this transaction.");
    }

    // Deduct the transaction amount and fee from sender's balance
    sender.setBalance(sender.getBalance() - totalDeduction);
    userRepository.save(sender);

    // Add the transaction amount to the receiver's balance
    User receiver = transaction.getReceiver();
    receiver.setBalance(receiver.getBalance() + transaction.getAmount());
    userRepository.save(receiver);

    // Save the transaction
    transactionRepository.save(transaction);
  }
}
