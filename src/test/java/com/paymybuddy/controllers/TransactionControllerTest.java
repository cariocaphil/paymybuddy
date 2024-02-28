package com.paymybuddy.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.models.Transaction;
import com.paymybuddy.models.WithdrawRequest;
import com.paymybuddy.services.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.eq;
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

  @Test
  public void withdrawToBank_SuccessfulWithdrawal_ReturnsSuccessMessage() throws Exception {
    WithdrawRequest withdrawRequest = new WithdrawRequest(1L, 100.0);

    mockMvc.perform(post("/api/v1/transactions/withdraw-to-bank")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(withdrawRequest)))
        .andExpect(status().isOk())
        .andExpect(content().string("Withdrawal successful"));
  }

  @Test
  public void withdrawToBank_InsufficientFunds_ReturnsBadRequest() throws Exception {
    WithdrawRequest withdrawRequest = new WithdrawRequest(1L, 5000.0); // Large amount to simulate insufficient funds

    // Adjusting the mock to throw an exception for the specific userId and amount
    doThrow(new IllegalStateException("Insufficient funds for withdrawal."))
        .when(transactionService).withdrawToBank(eq(1L), eq(5000.0));

    mockMvc.perform(post("/api/v1/transactions/withdraw-to-bank")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(withdrawRequest)))
        .andExpect(status().isBadRequest()) // Verifying that we get a bad request response
        .andExpect(content().string(containsString("Insufficient funds for withdrawal.")));
  }

  @Test
  public void withdrawToBank_UserNotFound_ReturnsBadRequest() throws Exception {
    WithdrawRequest withdrawRequest = new WithdrawRequest(999L, 100.0); // Non-existent user ID to simulate user not found

    doThrow(new IllegalArgumentException("User not found with ID: " + withdrawRequest.getUserId()))
        .when(transactionService).withdrawToBank(withdrawRequest.getUserId(), withdrawRequest.getAmount());

    mockMvc.perform(post("/api/v1/transactions/withdraw-to-bank")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(withdrawRequest)))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Error processing withdrawal: User not found with ID: " + withdrawRequest.getUserId()));
  }
}
