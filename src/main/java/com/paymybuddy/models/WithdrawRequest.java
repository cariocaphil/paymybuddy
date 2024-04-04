package com.paymybuddy.models;

import com.paymybuddy.models.Currency;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "currency")
public class WithdrawRequest {
  private long userId;
  private double amount;
  private Currency currency;

  // toString method for logging and debugging purposes
  @Override
  public String toString() {
    return "WithdrawRequest{" +
        "userId=" + userId +
        ", amount=" + amount +
        '}';
  }
}
