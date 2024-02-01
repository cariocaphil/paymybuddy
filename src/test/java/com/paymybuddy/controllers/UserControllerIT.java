package com.paymybuddy.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.models.LoadMoneyRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIT {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  public void getAllUsers_ShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/v1/users"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  public void getUserById_WithValidId_ShouldReturnUser() throws Exception {
    // Assuming you have a user with ID 1
    long userId = 1;
    mockMvc.perform(get("/api/v1/users/{userId}", userId))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.id").value(userId));
  }

  @Test
  public void addFriend_WithValidUserAndFriendId_ShouldReturnSuccess() throws Exception {
    Map<String, Long> friendRequest = new HashMap<>();
    friendRequest.put("userId", 1L);
    friendRequest.put("friendId", 2L);

    mockMvc.perform(post("/api/v1/users/add-friend")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(friendRequest)))
        .andExpect(status().isOk())
        .andExpect(content().string("Friend added successfully"));
  }

  @Test
  public void loadMoney_WithValidRequest_ShouldReturnSuccess() throws Exception {
    LoadMoneyRequest loadMoneyRequest = new LoadMoneyRequest();
    loadMoneyRequest.setUserId(1L);
    loadMoneyRequest.setAmount(100.0);

    mockMvc.perform(post("/api/v1/users/load-money")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(loadMoneyRequest)))
        .andExpect(status().isOk())
        .andExpect(content().string("Money loaded successfully"));
  }
}
