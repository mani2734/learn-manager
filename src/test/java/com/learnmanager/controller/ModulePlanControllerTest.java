package com.learnmanager.controller;

import com.learnmanager.service.ModulePlanService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ModulePlanControllerTest extends AbstractControllerTest {

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new ModulePlanController(mock(ModulePlanService.class)));
  }

  @Test
  void createShouldReturnCreated() throws Exception {
    mockMvc.perform(json(post("/api/modulePlans/create")).principal(authentication())).andExpect(status().isCreated());
  }

  @Test
  void getAllShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/modulePlans/getAll").principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void getAllByPlanningPeriodShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/modulePlans/getAllByPlanningPeriod/{planningPeriodId}", 1L).principal(authentication()))
           .andExpect(status().isOk());
  }

  @Test
  void getByIdShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/modulePlans/get/{id}", 1L).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void updateShouldReturnOk() throws Exception {
    mockMvc.perform(json(put("/api/modulePlans/update/{id}", 1L)).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void deleteShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/modulePlans/delete/{id}", 1L).principal(authentication())).andExpect(status().isNoContent());
  }
}
