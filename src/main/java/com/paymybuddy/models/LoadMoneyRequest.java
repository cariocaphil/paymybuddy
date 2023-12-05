package com.paymybuddy.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoadMoneyRequest {
  private long userId;
  private double amount;

}