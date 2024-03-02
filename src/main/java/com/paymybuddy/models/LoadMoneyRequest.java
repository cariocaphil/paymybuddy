package com.paymybuddy.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadMoneyRequest {
  private long userId;
  private double amount;
  private Currency currency; // Use the Currency enum here

  public LoadMoneyRequest(long userId, double amount) {
  }

  public LoadMoneyRequest() {

  }
}