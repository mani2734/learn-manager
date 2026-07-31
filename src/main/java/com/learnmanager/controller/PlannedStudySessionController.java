package com.learnmanager.controller;

import com.learnmanager.dto.request.create.CreatePlannedStudySessionRequest;
import com.learnmanager.dto.request.create.CreatePlannedStudySessionSeriesRequest;
import com.learnmanager.dto.request.update.UpdatePlannedStudySessionRequest;
import com.learnmanager.dto.response.PlannedStudySessionResponse;
import com.learnmanager.service.PlannedStudySessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plannedStudySessions/")
@RequiredArgsConstructor
public class PlannedStudySessionController {

  private final PlannedStudySessionService plannedStudySessionService;

  @PostMapping("create")
  @ResponseStatus(HttpStatus.CREATED)
  public PlannedStudySessionResponse create(Authentication authentication, @Valid @RequestBody CreatePlannedStudySessionRequest request) {
    return plannedStudySessionService.create(authentication.getName(), request);
  }

  @GetMapping("getAll")
  public List<PlannedStudySessionResponse> getAll(Authentication authentication) {
    return plannedStudySessionService.getAll(authentication.getName());
  }

  @GetMapping("getAllByStudyModule/{studyModuleId}")
  public List<PlannedStudySessionResponse> getAllByStudyModule(Authentication authentication, @PathVariable Long studyModuleId) {
    return plannedStudySessionService.getAllByStudyModule(authentication.getName(), studyModuleId);
  }

  @GetMapping("get/{id}")
  public PlannedStudySessionResponse getById(Authentication authentication, @PathVariable Long id) {
    return plannedStudySessionService.getById(authentication.getName(), id);
  }

  @PutMapping("update/{id}")
  public PlannedStudySessionResponse update(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody UpdatePlannedStudySessionRequest request) {
    return plannedStudySessionService.update(authentication.getName(), id, request);
  }

  @DeleteMapping("delete/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(Authentication authentication, @PathVariable Long id) {
    plannedStudySessionService.delete(authentication.getName(), id);
  }

  @PostMapping("createSeries")
  @ResponseStatus(HttpStatus.CREATED)
  public List<PlannedStudySessionResponse> createSeries(
      Authentication authentication,
      @Valid @RequestBody CreatePlannedStudySessionSeriesRequest request) {
    return plannedStudySessionService.createSeries(authentication.getName(), request);
  }

}