package com.paymybuddy.services;

import com.paymybuddy.dto.TransactionDTO;
import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.User;
import com.paymybuddy.models.WithdrawRequest;
import com.paymybuddy.repository.TransactionRepository;
import com.paymybuddy.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

  public List<TransactionDTO> getTransactionsForUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("User not found."));

    // Fetch transactions where the user is the sender
    List<Transaction> sentTransactions = transactionRepository.findAllBySender(user);

    // Fetch transactions where the user is the receiver
    List<Transaction> receivedTransactions = transactionRepository.findAllByReceiver(user);

    // Combine both lists and convert to DTOs
    return Stream.concat(sentTransactions.stream(), receivedTransactions.stream())
        .distinct()
        .map(this::convertToTransactionDTO)
        .collect(Collectors.toList());
  }


  public Transaction getTransactionById(Long transactionId) {
    return transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found."));
  }

  public TransactionDTO convertToTransactionDTO(Transaction transaction) {
    TransactionDTO dto = new TransactionDTO();
    dto.setTransactionId(transaction.getTransactionID());
    dto.setAmount(transaction.getAmount());
    dto.setTimestamp(transaction.getTimestamp());
    dto.setDescription(transaction.getDescription());
    dto.setFee(transaction.getFee());
    // Handle potential null value for currency
    if (transaction.getCurrency() != null) {
      dto.setCurrency(transaction.getCurrency().toString());
    } else {
      dto.setCurrency("");
    }
    if (transaction.getSender() != null) {
      dto.setSenderId(transaction.getSender().getUserID());

    } else {
      dto.setSenderId(0);
    }
    if (transaction.getReceiver() != null) {
      dto.setReceiverId(transaction.getReceiver().getUserID());
    } else {
      dto.setReceiverId(0);
    }
    return dto;
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

  public Page<TransactionDTO> getTransactionsForUserPageable(Long userId, Pageable pageable) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));

    Page<Transaction> transactionPage = transactionRepository.findBySenderOrReceiver(user, pageable);
    return transactionPage.map(this::convertToTransactionDTO);
  }
}
