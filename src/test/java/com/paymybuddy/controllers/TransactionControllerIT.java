package com.paymybuddy.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void makePayment_WithValidTransaction_ShouldReturnSuccess() throws Exception {
    Transaction transaction = new Transaction();
    transaction.setAmount(50.0);
    transaction.setFee(5.0);

    // Mock sender and receiver
    User sender = new User();
    sender.setUserID(1L);
    User receiver = new User();
    receiver.setUserID(2L);

    transaction.setSender(sender);
    transaction.setReceiver(receiver);

    mockMvc.perform(post("/api/v1/transactions/make-payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(transaction)))
        .andExpect(status().isOk())
        .andExpect(content().string("Payment successful"));
  }

  @Test
  public void makePayment_WithInvalidTransaction_ShouldReturnError() throws Exception {
    // Assuming invalid scenario, like insufficient funds or non-existing user IDs
    Transaction transaction = new Transaction();
    transaction.setAmount(10000.0); // Assumed to be more than the sender's balance
    transaction.setFee(5.0);

    // Mock sender and receiver with potentially non-existing IDs for the test
    User sender = new User();
    sender.setUserID(999L); // Non-existing user
    User receiver = new User();
    receiver.setUserID(998L); // Non-existing user

    transaction.setSender(sender);
    transaction.setReceiver(receiver);

    mockMvc.perform(post("/api/v1/transactions/make-payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(transaction)))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Error during payment processing: Insufficient funds for this transaction."));
  }
}
