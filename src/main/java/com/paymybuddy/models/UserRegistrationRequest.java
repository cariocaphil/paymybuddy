package com.paymybuddy.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRegistrationRequest {
  private String email;
  private String socialMediaAcc;
  private double balance;
  private String currency;
  private String password;
  private String bankAccountNumber;
  private String bankName;
  private String bankRoutingNumber;

  public UserRegistrationRequest(String email, String password, String socialMediaAcc, double balance, String currency) {
    this.email = email;
    this.password = password;
    this.socialMediaAcc = socialMediaAcc;
    this.balance = balance;
    this.currency = currency;
  }
}