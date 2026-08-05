package com.learnmanager.controller;

import com.learnmanager.service.NotificationSettingsService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationSettingsControllerTest extends AbstractControllerTest {

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new NotificationSettingsController(mock(NotificationSettingsService.class)));
  }

  @Test
  void getShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/notificationSettings/get").principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void updateShouldReturnOk() throws Exception {
    mockMvc.perform(json(put("/api/notificationSettings/update")).principal(authentication())).andExpect(status().isOk());
  }
}
