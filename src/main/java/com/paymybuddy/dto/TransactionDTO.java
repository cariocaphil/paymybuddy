package com.paymybuddy.dto;

import java.time.LocalDateTime;

public class TransactionDTO {
  private long transactionId;
  private double amount;
  private LocalDateTime timestamp;
  private String description;
  private double fee;
  private String currency;
  private long senderId; // or senderName if you want to display the sender's name
  private long receiverId; // or receiverName if you want to display the receiver's name

  // Constructors, getters, and setters

  public TransactionDTO() {
  }

  public TransactionDTO(long transactionId, double amount, LocalDateTime timestamp, String description, double fee, String currency, long senderId, long receiverId) {
    this.transactionId = transactionId;
    this.amount = amount;
    this.timestamp = timestamp;
    this.description = description;
    this.fee = fee;
    this.currency = currency;
    this.senderId = senderId;
    this.receiverId = receiverId;
  }

  // Getters and setters for all properties
  public long getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(long transactionId) {
    this.transactionId = transactionId;
  }

  public double getAmount() {
    return amount;
  }

  public void setAmount(double amount) {
    this.amount = amount;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public double getFee() {
    return fee;
  }

  public void setFee(double fee) {
    this.fee = fee;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public long getSenderId() {
    return senderId;
  }

  public void setSenderId(long senderId) {
    this.senderId = senderId;
  }

  public long getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(long receiverId) {
    this.receiverId = receiverId;
  }
}
