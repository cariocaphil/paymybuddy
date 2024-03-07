package com.paymybuddy.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequest {
  private String email;
  private String socialMediaAcc;
  private double balance;
  private String currency;
  private String password;
  private String bankAccountNumber;
  private String bankName;
  private String bankRoutingNumber;
}