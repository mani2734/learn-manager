package com.learnmanager.controller;

import com.learnmanager.service.NotificationService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NotificationControllerTest extends AbstractControllerTest {

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new NotificationController(mock(NotificationService.class)));
  }

  @Test
  void getAllShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/notifications/getAll").principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void getUnreadShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/notifications/getUnread").principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void getByIdShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/notifications/get/{id}", 1L).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void markAsReadShouldReturnOk() throws Exception {
    mockMvc.perform(put("/api/notifications/markAsRead/{id}", 1L).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void markAllAsReadShouldReturnNoContent() throws Exception {
    mockMvc.perform(put("/api/notifications/markAllAsRead").principal(authentication())).andExpect(status().isNoContent());
  }
}
