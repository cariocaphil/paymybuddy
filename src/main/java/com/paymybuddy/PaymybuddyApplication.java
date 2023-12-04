package com.paymybuddy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.paymybuddy.repository")
@EntityScan("com.paymybuddy.models")
@ComponentScan("com.paymybuddy")
public class PaymybuddyApplication {
  public static void main(String[] args) {
    SpringApplication.run(PaymybuddyApplication.class, args);
  }
}