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

    double transactionAmount = transaction.getAmount();
    if (!sender.getCurrency().equals(receiver.getCurrency())) {
      transactionAmount = currencyConversionService.convertCurrency(transaction.getAmount(), sender.getCurrency(), receiver.getCurrency());
      Logger.info("Converted transaction amount from {} {} to {} {}", transaction.getAmount(), sender.getCurrency(), transactionAmount, receiver.getCurrency());
    }

    double fee = transaction.getAmount() * 0.005;
    double totalDeduction = transaction.getAmount() + fee;
    if (sender.getBalance() < totalDeduction) {
      throw new IllegalStateException("Insufficient funds for this transaction.");
    }

    transaction.setFee(fee);
    sender.setBalance(sender.getBalance() - totalDeduction);
    receiver.setBalance(receiver.getBalance() + transactionAmount);
    userRepository.save(sender);
    userRepository.save(receiver);
    transaction.setTimestamp(LocalDateTime.now());
    transactionRepository.save(transaction);
  }

  public Transaction getTransactionById(Long transactionId) {
    Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));
    return transaction;
  }

  @Transactional
  public void withdrawToBank(WithdrawRequest withdrawRequest) {
    String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    User authenticatedUser = userRepository.findByEmail(authenticatedUsername)
        .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"));

    if (withdrawRequest.getUserId() != authenticatedUser.getUserID()) {
      throw new IllegalStateException("Withdrawals can only be made from the authenticated user's account.");
    }

    double amountToWithdraw = withdrawRequest.getAmount();
    if (withdrawRequest.getCurrency() != authenticatedUser.getCurrency()) {
      amountToWithdraw = currencyConversionService.convertCurrency(withdrawRequest.getAmount(), withdrawRequest.getCurrency(), authenticatedUser.getCurrency());
      Logger.info("Converted withdrawal amount to user's currency: {} {}", amountToWithdraw, authenticatedUser.getCurrency());
    }

    if (authenticatedUser.getBalance() < amountToWithdraw) {
      throw new IllegalStateException("Insufficient funds for withdrawal.");
    }

    authenticatedUser.setBalance(authenticatedUser.getBalance() - amountToWithdraw);
    userRepository.save(authenticatedUser);

    Transaction withdrawalTransaction = new Transaction();
    withdrawalTransaction.setAmount(-amountToWithdraw);
    withdrawalTransaction.setCurrency(authenticatedUser.getCurrency());
    withdrawalTransaction.setTimestamp(LocalDateTime.now());
    withdrawalTransaction.setDescription("Withdrawal to bank account");
    withdrawalTransaction.setFee(0); // Assuming no fee for withdrawal
    withdrawalTransaction.setSender(authenticatedUser); // User is both sender and receiver
    withdrawalTransaction.setReceiver(null); // No external receiver for a withdrawal
    transactionRepository.save(withdrawalTransaction);
    Logger.info("Withdrawal to bank completed successfully for user ID: {}", withdrawRequest.getUserId());
  }
}
