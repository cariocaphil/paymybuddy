package com.paymybuddy.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
  private long transactionId;
  private double amount;
  private LocalDateTime timestamp;
  private String description;
  private double fee;
  private String currency;
  private long senderId;
  private long receiverId;

}
