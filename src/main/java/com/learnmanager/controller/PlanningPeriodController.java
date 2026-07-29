package com.learnmanager.controller;

import com.learnmanager.dto.CreatePlanningPeriodRequest;
import com.learnmanager.dto.PlanningPeriodResponse;
import com.learnmanager.service.PlanningPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planningPeriods/")
@RequiredArgsConstructor
public class PlanningPeriodController {

  private final PlanningPeriodService planningPeriodService;

  @PostMapping("create")
  @ResponseStatus(HttpStatus.CREATED)
  public PlanningPeriodResponse create(Authentication authentication, @Valid @RequestBody CreatePlanningPeriodRequest request) {
    return planningPeriodService.create(authentication.getName(), request);
  }
}