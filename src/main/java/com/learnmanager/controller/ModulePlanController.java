package com.learnmanager.controller;

import com.learnmanager.dto.request.create.CreateModulePlanRequest;
import com.learnmanager.dto.request.update.UpdateModulePlanRequest;
import com.learnmanager.dto.response.ModulePlanResponse;
import com.learnmanager.service.ModulePlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modulePlans/")
@RequiredArgsConstructor
public class ModulePlanController {

  private final ModulePlanService modulePlanService;

  @PostMapping("create")
  @ResponseStatus(HttpStatus.CREATED)
  public ModulePlanResponse create(Authentication authentication, @Valid @RequestBody CreateModulePlanRequest request) {
    return modulePlanService.create(authentication.getName(), request);
  }

  @GetMapping("getAll")
  public List<ModulePlanResponse> getAll(Authentication authentication) {
    return modulePlanService.getAll(authentication.getName());
  }

  @GetMapping("getAllByPlanningPeriod/{planningPeriodId}")
  public List<ModulePlanResponse> getAllByPlanningPeriod(Authentication authentication, @PathVariable Long planningPeriodId) {
    return modulePlanService.getAllByPlanningPeriod(authentication.getName(), planningPeriodId);
  }

  @GetMapping("get/{id}")
  public ModulePlanResponse getById(Authentication authentication, @PathVariable Long id) {
    return modulePlanService.getById(authentication.getName(), id);
  }

  @PutMapping("update/{id}")
  public ModulePlanResponse update(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody UpdateModulePlanRequest request) {
    return modulePlanService.update(authentication.getName(), id, request);
  }

  @DeleteMapping("delete/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(Authentication authentication, @PathVariable Long id) {
    modulePlanService.delete(authentication.getName(), id);
  }
}