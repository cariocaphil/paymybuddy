package com.paymybuddy.controllers;

import com.paymybuddy.models.Transaction;
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
}
