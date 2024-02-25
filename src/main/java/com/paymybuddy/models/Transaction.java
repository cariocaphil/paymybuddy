package com.paymybuddy.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime; // Import the LocalDateTime class

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "transaction_table")
@ToString
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // Assuming IDENTITY for simplicity
  private long transactionID;

  private double amount;

  private LocalDateTime timestamp;

  private String description;

  private double fee;

  @ManyToOne
  @JoinColumn(name = "sender_userid")
  private User sender;

  @ManyToOne
  @JoinColumn(name = "receiver_userid")
  private User receiver;
}