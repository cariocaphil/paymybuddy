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

  @Autowired
  private CurrencyConversionService currencyConversionService;

  @Transactional
  public void makePayment(Transaction transaction) {
    Logger.info("Attempting to process payment for transaction: {}", transaction);
    User sender = userRepository.findById(transaction.getSender().getUserID())
        .orElseThrow(() -> new IllegalArgumentException("Sender not found."));
    User receiver = userRepository.findById(transaction.getReceiver().getUserID())
        .orElseThrow(() -> new IllegalArgumentException("Receiver not found."));

    // Check currency and convert if necessary
    double transactionAmount = transaction.getAmount();
    if (!sender.getCurrency().equals(receiver.getCurrency())) {
      transactionAmount = currencyConversionService.convertCurrency(transaction.getAmount(), sender.getCurrency(), receiver.getCurrency());
      Logger.info("Converted transaction amount from {} {} to {} {}", transaction.getAmount(), sender.getCurrency(), transactionAmount, receiver.getCurrency());
    }

    // Calculate the fee in sender's currency
    double fee = transaction.getAmount() * 0.005;
    double totalDeduction = transaction.getAmount() + fee;

    if (sender.getBalance() < totalDeduction) {
      Logger.error("Insufficient funds for transaction: {}", transaction);
      throw new IllegalStateException("Insufficient funds for this transaction.");
    }

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
  public void withdrawToBank(WithdrawRequest withdrawRequest) {
    Logger.info("Initiating withdrawal to bank for user ID: {} and amount: {} {}", withdrawRequest.getUserId(), withdrawRequest.getAmount(), withdrawRequest.getCurrency());
    User user = userRepository.findById(withdrawRequest.getUserId())
        .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + withdrawRequest.getUserId()));

    // Convert the withdrawal amount to the user's currency if necessary
    double amountToWithdraw = withdrawRequest.getAmount();
    if (withdrawRequest.getCurrency() != user.getCurrency()) {
      amountToWithdraw = currencyConversionService.convertCurrency(withdrawRequest.getAmount(), withdrawRequest.getCurrency(), user.getCurrency());
      Logger.info("Converted withdrawal amount to user's currency: {} {}", amountToWithdraw, user.getCurrency());
    }

    // Check if the user has enough balance to cover the withdrawal
    if (user.getBalance() < amountToWithdraw) {
      Logger.error("Insufficient funds for withdrawal for user ID: {}", withdrawRequest.getUserId());
      throw new IllegalStateException("Insufficient funds for withdrawal.");
    }

    // Deduct the withdrawal amount from the user's balance
    double newBalance = user.getBalance() - amountToWithdraw;
    user.setBalance(newBalance);
    userRepository.save(user);

    // Record the withdrawal as a transaction
    Transaction transaction = new Transaction();
    transaction.setAmount(-amountToWithdraw); // Negative to indicate withdrawal
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
    Logger.info("Withdrawal to bank completed successfully for user ID: {}", withdrawRequest.getUserId());
  }

}
