package com.learnmanager.controller;

import com.learnmanager.dto.ReportingDashboardResponse;
import com.learnmanager.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reporting/")
@RequiredArgsConstructor
public class ReportingController {

  private final ReportingService reportingService;

  @GetMapping("getDashboard")
  public ReportingDashboardResponse getDashboard(Authentication authentication) {
    return reportingService.getDashboard(authentication.getName());
  }
}