package com.paymybuddy.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "user_table")
@ToString
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class User {

  public User(long userId, String s, String twitter, double v) {
  }

  public User(long userId, String s, SocialMediaAccount twitter, double v) {
  }

  public enum SocialMediaAccount {
    Twitter,
    Facebook,
    // Add more social media accounts as needed
  }

  @Id
  @SequenceGenerator(name = "user_sequence", sequenceName = "user_sequence", allocationSize = 1)
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_sequence")
  private long userID;
  private String email;
  @Enumerated(EnumType.STRING)
  @Column(name = "socialmediaacc")
  private SocialMediaAccount socialMediaAcc;
  private double balance;
  private String password;

  @ManyToMany
  @JoinTable(
      name = "user_connections",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "connected_user_id")
  )
  private List<User> connections = new ArrayList<>();
}
