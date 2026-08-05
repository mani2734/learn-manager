package com.learnmanager.controller;

import com.learnmanager.service.AuthenticationService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthenticationControllerTest extends AbstractControllerTest {

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new AuthenticationController(mock(AuthenticationService.class)));
  }

  @Test
  void registerShouldReturnCreated() throws Exception {
    mockMvc.perform(json(post("/api/auth/register"))).andExpect(status().isCreated());
  }

  @Test
  void loginShouldReturnOk() throws Exception {
    mockMvc.perform(json(post("/api/auth/login"))).andExpect(status().isOk());
  }

  @Test
  void getCurrentUserShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/auth/me").principal(authentication())).andExpect(status().isOk());
  }
}
