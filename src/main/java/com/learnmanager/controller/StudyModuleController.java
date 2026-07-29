package com.learnmanager.controller;

import com.learnmanager.dto.CreateStudyModuleRequest;
import com.learnmanager.dto.StudyModuleResponse;
import com.learnmanager.service.StudyModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/modules/")
@RequiredArgsConstructor
public class StudyModuleController {

  private final StudyModuleService studyModuleService;

  @PostMapping("create")
  @ResponseStatus(HttpStatus.CREATED)
  public StudyModuleResponse create(Authentication authentication, @Valid @RequestBody CreateStudyModuleRequest request) {
    return studyModuleService.create(authentication.getName(), request);
  }
}