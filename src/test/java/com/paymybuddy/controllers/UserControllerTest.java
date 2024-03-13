package com.paymybuddy.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paymybuddy.models.Currency;
import com.paymybuddy.models.LoadMoneyRequest;
import com.paymybuddy.models.User;
import com.paymybuddy.services.UserService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.List;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserControllerTest {

  private MockMvc mockMvc;

  @Mock
  private UserService userService;

  @InjectMocks
  private UserController userController;

  @BeforeEach
  public void setup() {
    initMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
  }

  @Test
  public void getAllUsersTest() throws Exception {
    List<User> users = Arrays.asList(new User(), new User());
    when(userService.getAllUsers()).thenReturn(users);

    mockMvc.perform(get("/api/v1/users")
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.length()").value(users.size()));
  }

  @Test
  public void getUserByIdTest() throws Exception {
    long userId = 1L;
    String userEmail = "user@example.com";
    User user = new User();
    user.setEmail(userEmail);
    when(userService.getUserById(userId)).thenReturn(Optional.of(user));

    mockMvc.perform(get("/api/v1/users/" + userId)
            .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(userEmail));
  }


  @Test
  public void addFriendTest() throws Exception {
    long userId = 1L;
    long friendId = 2L;

    mockMvc.perform(post("/api/v1/users/add-friend")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\": " + userId + ", \"friendId\": " + friendId + "}"))
        .andExpect(status().isOk())
        .andExpect(content().string("Friend added successfully"));
  }

  @Test
  public void loadMoneyTest() throws Exception {
    long userId = 1L;
    double amount = 100.0;

    mockMvc.perform(post("/api/v1/users/load-money")
            .contentType(MediaType.APPLICATION_JSON)
            .content(new ObjectMapper().writeValueAsString(new LoadMoneyRequest(userId, amount, Currency.EUR))))
        .andExpect(status().isOk())
        .andExpect(content().string("Money loaded successfully"));
  }

}
