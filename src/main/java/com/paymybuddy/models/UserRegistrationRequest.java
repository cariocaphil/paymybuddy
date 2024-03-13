package com.paymybuddy.models;

import lombok.AllArgsConstructor;
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

  public UserRegistrationRequest(String s, String password, String twitter, double v, String usd) {
  }

  public UserRegistrationRequest(){

  }
}