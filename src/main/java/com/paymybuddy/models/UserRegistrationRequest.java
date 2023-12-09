package com.paymybuddy.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequest {
  private String email;
  private String socialMediaAcc;
  private double balance;

}