package com.learnmanager.controller;

import com.learnmanager.service.StudyModuleService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StudyModuleControllerTest extends AbstractControllerTest {

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new StudyModuleController(mock(StudyModuleService.class)));
  }

  @Test
  void createShouldReturnCreated() throws Exception {
    mockMvc.perform(json(post("/api/modules/create")).principal(authentication())).andExpect(status().isCreated());
  }

  @Test
  void getAllShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/modules/getAll").principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void getByIdShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/modules/get/{id}", 1L).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void updateShouldReturnOk() throws Exception {
    mockMvc.perform(json(put("/api/modules/update/{id}", 1L)).principal(authentication())).andExpect(status().isOk());
  }

  @Test
  void deleteShouldReturnNoContent() throws Exception {
    mockMvc.perform(delete("/api/modules/delete/{id}", 1L).principal(authentication())).andExpect(status().isNoContent());
  }
}
