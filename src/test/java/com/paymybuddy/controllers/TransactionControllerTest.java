package com.paymybuddy.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.models.Transaction;
import com.paymybuddy.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TransactionControllerTest {

  private MockMvc mockMvc;

  @Mock
  private TransactionService transactionService;

  @InjectMocks
  private TransactionController transactionController;

  @BeforeEach
  public void setup() {
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
  }

  @Test
  public void makePayment_SuccessfulTransaction_ReturnsSuccessMessage() throws Exception {
    Transaction transaction = new Transaction();
    transaction.setAmount(100.0);
    transaction.setFee(2.0);
    // Assume sender and receiver are set up properly

    mockMvc.perform(post("/api/v1/transactions/make-payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(transaction)))
        .andExpect(status().isOk())
        .andExpect(content().string("Payment successful"));
  }

  @Test
  public void makePayment_InsufficientFunds_ReturnsErrorMessage() throws Exception {
    Transaction transaction = new Transaction();
    transaction.setAmount(100.0);
    transaction.setFee(2.0);
    // Assume sender and receiver are set up properly

    doThrow(new IllegalStateException("Insufficient funds for this transaction."))
        .when(transactionService).makePayment(transaction);

    mockMvc.perform(post("/api/v1/transactions/make-payment")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(transaction)))
        .andExpect(status().isInternalServerError())
        .andExpect(content().string("Error during payment processing: Insufficient funds for this transaction."));
  }

  @Test
  public void getTransactionDetails_WithValidId_ReturnsTransaction() throws Exception {
    long transactionId = 1L;
    Transaction transaction = new Transaction();
    transaction.setTransactionID(transactionId);
    transaction.setAmount(100.0);
    transaction.setFee(2.0);
    // Assume sender and receiver are set up properly

    when(transactionService.getTransactionById(transactionId)).thenReturn(transaction);

    mockMvc.perform(get("/api/v1/transactions/{transactionId}", transactionId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.transactionID").value(transactionId))
        .andExpect(jsonPath("$.amount").value(100.0))
        .andExpect(jsonPath("$.fee").value(2.0));
  }

  @Test
  public void getTransactionDetails_WithInvalidId_ReturnsNotFound() throws Exception {
    long invalidTransactionId = 999L;

    when(transactionService.getTransactionById(invalidTransactionId))
        .thenThrow(new IllegalArgumentException("Transaction not found."));

    mockMvc.perform(get("/api/v1/transactions/{transactionId}", invalidTransactionId)
            .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound());
  }
}
