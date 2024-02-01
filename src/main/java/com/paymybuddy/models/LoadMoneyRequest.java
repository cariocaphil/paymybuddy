package com.paymybuddy.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadMoneyRequest {
  private long userId;
  private double amount;

  public LoadMoneyRequest(long userId, double amount) {
  }

  public LoadMoneyRequest() {

  }
}