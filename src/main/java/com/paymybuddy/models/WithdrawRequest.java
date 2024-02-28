package com.paymybuddy.models;

public class WithdrawRequest {

  private Long userId; // or String, depending on how you identify users
  private double amount;

  // Default constructor
  public WithdrawRequest() {
  }

  // Constructor with fields
  public WithdrawRequest(Long userId, double amount) {
    this.userId = userId;
    this.amount = amount;
  }

  // Getters and setters
  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
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
