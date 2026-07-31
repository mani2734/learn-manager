package com.learnmanager.controller;

import com.learnmanager.dto.request.create.CreateStudyModuleRequest;
import com.learnmanager.dto.request.update.UpdateStudyModuleRequest;
import com.learnmanager.dto.response.StudyModuleResponse;
import com.learnmanager.service.StudyModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

  @GetMapping("getAll")
  public List<StudyModuleResponse> getAll(
      Authentication authentication) {
    return studyModuleService.getAll(authentication.getName());
  }

  @GetMapping("get/{id}")
  public StudyModuleResponse getById(Authentication authentication, @PathVariable Long id) {
    return studyModuleService.getById(authentication.getName(), id);
  }

  @PutMapping("update/{id}")
  public StudyModuleResponse update(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody UpdateStudyModuleRequest request) {
    return studyModuleService.update(authentication.getName(), id, request);
  }

  @DeleteMapping("delete/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(Authentication authentication, @PathVariable Long id) {
    studyModuleService.delete(authentication.getName(), id);
  }
}