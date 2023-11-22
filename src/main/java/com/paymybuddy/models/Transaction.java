package com.paymybuddy.models;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
  private long transactionID;
  private double amount;
  private long timestamp;
  private String description;
  private double fee;
  private User sender;
  private User receiver;
}
