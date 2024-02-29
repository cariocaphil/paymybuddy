package com.paymybuddy.controllers;

import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.WithdrawRequest;
import com.paymybuddy.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tinylog.Logger;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

  @Autowired
  private TransactionService transactionService;

  @PostMapping("/make-payment")
  public ResponseEntity<String> makePayment(@RequestBody Transaction transaction) {
    Logger.info("Received request to make a payment: {}", transaction);
    try {
      transactionService.makePayment(transaction);
      Logger.info("Payment processed successfully for transaction: {}", transaction.getTransactionID());
      return ResponseEntity.ok("Payment successful");
    } catch (Exception e) {
      Logger.error("Error during payment processing for transaction: {}", transaction.getTransactionID(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error during payment processing: " + e.getMessage());
    }
  }

  @GetMapping("/{transactionId}")
  public ResponseEntity<Transaction> getTransaction(@PathVariable Long transactionId) {
    Logger.info("Received request to get transaction details for ID: {}", transactionId);
    try {
      Transaction transaction = transactionService.getTransactionById(transactionId);
      Logger.info("Transaction details retrieved successfully for ID: {}", transactionId);
      return ResponseEntity.ok(transaction);
    } catch (Exception e) {
      Logger.error("Transaction not found with ID: {}", transactionId, e);
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(null); // Optionally provide a specific error message
    }
  }

  @PostMapping("/withdraw-to-bank")
  public ResponseEntity<String> withdrawToBank(@RequestBody WithdrawRequest withdrawRequest) {
    Logger.info("Received withdrawal request for user ID: {} with amount: {}", withdrawRequest.getUserId(), withdrawRequest.getAmount());
    try {
      transactionService.withdrawToBank(withdrawRequest.getUserId(), withdrawRequest.getAmount());
      Logger.info("Withdrawal successful for user ID: {}", withdrawRequest.getUserId());
      return ResponseEntity.ok("Withdrawal successful");
    } catch (IllegalArgumentException | IllegalStateException e) {
      Logger.error("Error processing withdrawal for user ID: {}", withdrawRequest.getUserId(), e);
      return ResponseEntity.badRequest().body("Error processing withdrawal: " + e.getMessage());
    } catch (Exception e) {
      Logger.error("Unexpected error during withdrawal for user ID: {}", withdrawRequest.getUserId(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Unexpected error processing withdrawal: " + e.getMessage());
    }
  }
}