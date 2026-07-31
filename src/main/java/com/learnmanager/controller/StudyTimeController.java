package com.learnmanager.controller;

import com.learnmanager.dto.CreateStudyTimeRequest;
import com.learnmanager.dto.StudyTimeResponse;
import com.learnmanager.service.StudyTimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/studyTimes/")
@RequiredArgsConstructor
public class StudyTimeController {

  private final StudyTimeService studyTimeService;

  @PostMapping("create")
  @ResponseStatus(HttpStatus.CREATED)
  public StudyTimeResponse create(Authentication authentication, @Valid @RequestBody CreateStudyTimeRequest request) {
    return studyTimeService.create(authentication.getName(), request);
  }
}