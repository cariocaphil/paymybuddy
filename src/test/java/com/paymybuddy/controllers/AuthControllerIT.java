package com.paymybuddy.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.models.UserRegistrationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void registerUser_ShouldReturnSuccessMessage() throws Exception {
    UserRegistrationRequest request = new UserRegistrationRequest();
    request.setEmail("testuser@example.com");
    request.setPassword("password");
    request.setSocialMediaAcc("FACEBOOK");
    request.setBalance(0.0);

    mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(content().string("User registered successfully"));
  }

  @Test
  public void registerUser_WithMissingEmail_ShouldReturnBadRequest() throws Exception {
    UserRegistrationRequest request = new UserRegistrationRequest();
    // Omitting the email field to simulate an invalid request
    request.setPassword("password");
    request.setSocialMediaAcc("FACEBOOK");
    request.setBalance(0.0);

    mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void registerUser_WithExistingEmail_ShouldReturnBadRequest() throws Exception {
    // First registration attempt
    UserRegistrationRequest firstRequest = new UserRegistrationRequest();
    firstRequest.setEmail("existinguser@example.com");
    firstRequest.setPassword("password");
    firstRequest.setSocialMediaAcc("FACEBOOK");
    firstRequest.setBalance(0.0);

    mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(firstRequest)))
        .andExpect(status().isOk());

    // Second registration attempt with the same email
    UserRegistrationRequest secondRequest = new UserRegistrationRequest();
    secondRequest.setEmail("existinguser@example.com"); // Same email as before
    secondRequest.setPassword("newpassword");
    secondRequest.setSocialMediaAcc("TWITTER"); // Different social media account
    secondRequest.setBalance(0.0);

    mockMvc.perform(post("/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(secondRequest)))
        .andExpect(status().isBadRequest()); // Expecting BadRequest due to duplicate email
  }

}
