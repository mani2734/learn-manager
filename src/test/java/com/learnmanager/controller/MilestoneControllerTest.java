package com.learnmanager.controller;

import com.learnmanager.service.MilestoneService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MilestoneControllerTest extends AbstractControllerTest {

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new MilestoneController(mock(MilestoneService.class)));
  }

  @Test
  void createShouldReturnCreated() throws Exception {
    mockMvc.perform(json(post("/api/milestones/create")).principal(authentication())).andExpect(status().isCreated());
  }

  @Test
  void getAllShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/milestones/getAll").principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void getAllByLearningGoalShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/milestones/getAllByLearningGoal/{learningGoalId}", 1L).principal(authentication()))
           .andExpect(status().isOk());
  }

  @Test
  void getByIdShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/milestones/get/{id}", 1L).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void updateShouldReturnOk() throws Exception {
    mockMvc.perform(json(put("/api/milestones/update/{id}", 1L)).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void deleteShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/milestones/delete/{id}", 1L).principal(authentication())).andExpect(status().isNoContent());
  }
}
