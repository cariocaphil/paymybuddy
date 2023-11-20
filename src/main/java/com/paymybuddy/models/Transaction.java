package com.paymybuddy.models;

public class Transaction {
  private long transactionID;
  private double amount;
  private long timestamp;
  private String description;
  private double fee;
  private User sender;
  private User receiver;
}
