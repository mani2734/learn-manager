package com.learnmanager.controller;

import com.learnmanager.service.PlannedStudySessionService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlannedStudySessionControllerTest extends AbstractControllerTest {

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new PlannedStudySessionController(mock(PlannedStudySessionService.class)));
  }

  @Test
  void createShouldReturnCreated() throws Exception {
    mockMvc.perform(json(post("/api/plannedStudySessions/create")).principal(authentication())).andExpect(status().isCreated());
  }

  @Test
  void getAllShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/plannedStudySessions/getAll").principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void getAllByStudyModuleShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/plannedStudySessions/getAllByStudyModule/{studyModuleId}", 1L).principal(authentication()))
           .andExpect(status().isOk());
  }

  @Test
  void getByIdShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/plannedStudySessions/get/{id}", 1L).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void updateShouldReturnOk() throws Exception {
    mockMvc.perform(json(put("/api/plannedStudySessions/update/{id}", 1L)).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void deleteShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/plannedStudySessions/delete/{id}", 1L).principal(authentication())).andExpect(status().isNoContent());
  }

  @Test
  void createSeriesShouldReturnCreated() throws Exception {
    mockMvc.perform(json(post("/api/plannedStudySessions/createSeries")).principal(authentication())).andExpect(status().isCreated());
  }
}
