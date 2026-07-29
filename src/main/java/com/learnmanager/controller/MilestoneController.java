package com.learnmanager.controller;

import com.learnmanager.dto.CreateMilestoneRequest;
import com.learnmanager.dto.MilestoneResponse;
import com.learnmanager.service.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/milestones/")
@RequiredArgsConstructor
public class MilestoneController {

  private final MilestoneService milestoneService;

  @PostMapping("create")
  @ResponseStatus(HttpStatus.CREATED)
  public MilestoneResponse create(Authentication authentication, @Valid @RequestBody CreateMilestoneRequest request) {
    return milestoneService.create(authentication.getName(), request);
  }
}