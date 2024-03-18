package com.paymybuddy.dto;

public class UserDTO {
  private Long userId;
  private String name; // or any other fields you want to include

  public UserDTO() {
  }

  public UserDTO(Long userId, String name) {
    this.userId = userId;
    this.name = name;
  }

  // Getters and setters
  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setEmail(String email) {
  }
}
