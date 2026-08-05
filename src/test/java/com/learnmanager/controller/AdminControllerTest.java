package com.learnmanager.controller;

import com.learnmanager.service.AdminService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminControllerTest extends AbstractControllerTest {

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new AdminController(mock(AdminService.class)));
  }

  @Test
  void getAllUsersShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/admin/users/getAll")).andExpect(status().isOk());
  }

  @Test
  void generateTestDataShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/admin/testData/generate")).andExpect(status().isOk());
  }

  @Test
  void getUserByIdShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/admin/users/get/{id}", 1L)).andExpect(status().isOk());
  }

  @Test
  void deactivateUserShouldReturnOk() throws Exception {
    mockMvc.perform(put("/api/admin/users/deactivate/{id}", 1L)).andExpect(status().isOk());
  }

  @Test
  void activateUserShouldReturnOk() throws Exception {
    mockMvc.perform(put("/api/admin/users/activate/{id}", 1L)).andExpect(status().isOk());
  }

  @Test
  void resetUserPasswordShouldReturnNoContent() throws Exception {
    mockMvc.perform(json(put("/api/admin/users/resetPassword/{id}", 1L))).andExpect(status().isNoContent());
  }
}
