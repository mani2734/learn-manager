package com.learnmanager.controller;

import com.learnmanager.dto.request.create.CreatePlanningPeriodRequest;
import com.learnmanager.dto.request.update.UpdatePlanningPeriodRequest;
import com.learnmanager.dto.response.PlanningPeriodResponse;
import com.learnmanager.service.PlanningPeriodService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

  @GetMapping("getAll")
  public List<PlanningPeriodResponse> getAll(Authentication authentication) {
    return planningPeriodService.getAll(authentication.getName());
  }

  @GetMapping("get/{id}")
  public PlanningPeriodResponse getById(Authentication authentication, @PathVariable Long id) {
    return planningPeriodService.getById(authentication.getName(), id);
  }

  @PutMapping("update/{id}")
  public PlanningPeriodResponse update(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody UpdatePlanningPeriodRequest request) {
    return planningPeriodService.update(authentication.getName(), id, request);
  }

  @DeleteMapping("delete/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(Authentication authentication, @PathVariable Long id) {
    planningPeriodService.delete(authentication.getName(), id);
  }
}