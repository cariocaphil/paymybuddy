package com.paymybuddy.models;

import com.paymybuddy.models.Currency;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WithdrawRequest {
  private long userId;
  private double amount;
  private Currency currency;

  // Constructors, getters, and setters
  public WithdrawRequest() {}

  public WithdrawRequest(long userId, double amount, Currency currency) {
    this.userId = userId;
    this.amount = amount;
    this.currency = currency;
  }

  // toString method for logging and debugging purposes
  @Override
  public String toString() {
    return "WithdrawRequest{" +
        "userId=" + userId +
        ", amount=" + amount +
        '}';
  }
}
