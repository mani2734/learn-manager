package com.learnmanager.controller;

import com.learnmanager.service.StudyTimeService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class StudyTimeControllerTest extends AbstractControllerTest {

  @Mock private StudyTimeService studyTimeService;

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new StudyTimeController(studyTimeService));
  }

  @Test
  void createShouldReturnCreated() throws Exception {
    mockMvc.perform(json(post("/api/studyTimes/create")).principal(authentication())).andExpect(status().isCreated());
  }

  @Test
  void getAllShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/studyTimes/getAll").principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void getAllByStudyModuleShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/studyTimes/getAllByStudyModule/{studyModuleId}", 1L).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void getAllByLearningGoalShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/studyTimes/getAllByLearningGoal/{learningGoalId}", 1L).principal(authentication()))
           .andExpect(status().isOk());
  }

  @Test
  void getByIdShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/studyTimes/get/{id}", 1L).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void updateShouldReturnOk() throws Exception {
    mockMvc.perform(json(put("/api/studyTimes/update/{id}", 1L)).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void deleteShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/studyTimes/delete/{id}", 1L).principal(authentication())).andExpect(status().isNoContent());
  }
}