package com.paymybuddy.controllers;

import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.WithdrawRequest;
import com.paymybuddy.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

  @Autowired
  private TransactionService transactionService;

  @PostMapping("/make-payment")
  public ResponseEntity<String> makePayment(@RequestBody Transaction transaction) {
    try {
      transactionService.makePayment(transaction);
      return ResponseEntity.ok("Payment successful");
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Error during payment processing: " + e.getMessage());
    }
  }

  @GetMapping("/{transactionId}")
  public ResponseEntity<Transaction> getTransaction(@PathVariable Long transactionId) {
    try {
      Transaction transaction = transactionService.getTransactionById(transactionId);
      return ResponseEntity.ok(transaction);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(null); // or provide a more specific error message if needed
    }
  }

  @PostMapping("/withdraw-to-bank")
  public ResponseEntity<String> withdrawToBank(@RequestBody WithdrawRequest withdrawRequest) {
    try {
      // Assuming WithdrawRequest contains a userId and amount field
      transactionService.withdrawToBank(withdrawRequest.getUserId(), withdrawRequest.getAmount());
      return ResponseEntity.ok("Withdrawal successful");
    } catch (IllegalArgumentException | IllegalStateException e) {
      return ResponseEntity.badRequest().body("Error processing withdrawal: " + e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Unexpected error processing withdrawal: " + e.getMessage());
    }
  }
}
