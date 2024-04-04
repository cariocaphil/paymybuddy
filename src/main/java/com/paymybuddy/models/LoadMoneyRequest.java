package com.paymybuddy.models;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoadMoneyRequest {
  private long userId;
  private double amount;
  private Currency currency;
}