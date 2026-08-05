package com.learnmanager.controller;

import com.learnmanager.service.PlanningPeriodService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlanningPeriodControllerTest extends AbstractControllerTest {

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new PlanningPeriodController(mock(PlanningPeriodService.class)));
  }

  @Test
  void createShouldReturnCreated() throws Exception {
    mockMvc.perform(json(post("/api/planningPeriods/create")).principal(authentication())).andExpect(status().isCreated());
  }

  @Test
  void getAllShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/planningPeriods/getAll").principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void getByIdShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/planningPeriods/get/{id}", 1L).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void updateShouldReturnOk() throws Exception {
    mockMvc.perform(json(put("/api/planningPeriods/update/{id}", 1L)).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void deleteShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/planningPeriods/delete/{id}", 1L).principal(authentication())).andExpect(status().isNoContent());
  }
}
