package com.learnmanager.controller;

import com.learnmanager.dto.CreatePlannedStudySessionRequest;
import com.learnmanager.dto.PlannedStudySessionResponse;
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

}