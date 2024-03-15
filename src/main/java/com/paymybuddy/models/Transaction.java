package com.paymybuddy.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;

import java.time.LocalDateTime; // Import the LocalDateTime class

import javax.persistence.SequenceGenerator;
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
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_sequence_generator")
  @SequenceGenerator(name = "transaction_sequence_generator", sequenceName = "transaction_sequence", allocationSize = 1)
  private long transactionID;

  private double amount;

  private LocalDateTime timestamp;

  private String description;

  private double fee;

  @Column(name = "currency")
  @Enumerated(EnumType.STRING)
  private Currency currency;

  @ManyToOne
  @JoinColumn(name = "sender_userid")
  @JsonIgnore
  private User sender;

  @ManyToOne
  @JoinColumn(name = "receiver_userid")
  @JsonIgnore
  private User receiver;
}