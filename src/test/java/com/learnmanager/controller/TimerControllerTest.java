package com.learnmanager.controller;

import com.learnmanager.service.TimerService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TimerControllerTest extends AbstractControllerTest {

  @Mock private TimerService timerService;

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new TimerController(timerService));
  }

  @Test
  void startShouldReturnCreated() throws Exception {
    mockMvc.perform(json(post("/api/timers/start")).principal(authentication())).andExpect(status().isCreated());
  }

  @Test
  void getActiveShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/timers/getActive").principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void stopShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/timers/stop").principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void cancelShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/timers/cancel").principal(authentication())).andExpect(status().isNoContent());
  }
}