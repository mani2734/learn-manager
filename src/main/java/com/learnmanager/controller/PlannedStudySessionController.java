package com.learnmanager.controller;

import com.learnmanager.dto.CreatePlannedStudySessionRequest;
import com.learnmanager.dto.PlannedStudySessionResponse;
import com.learnmanager.service.PlannedStudySessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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
}