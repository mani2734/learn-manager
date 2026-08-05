package com.learnmanager.controller;

import com.learnmanager.service.ReportingService;
import com.learnmanager.testsupport.AbstractControllerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportingControllerTest extends AbstractControllerTest {

  @BeforeEach
  void setUp() {
    mockMvc = buildMockMvc(new ReportingController(mock(ReportingService.class)));
  }

  @Test
  void getDashboardShouldReturnOk() throws Exception {
    mockMvc.perform(get("/api/reporting/getDashboard").principal(authentication())).andExpect(status().isOk());
  }
}
