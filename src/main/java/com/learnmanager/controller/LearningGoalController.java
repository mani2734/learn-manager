package com.learnmanager.controller;

import com.learnmanager.dto.request.create.CreateLearningGoalRequest;
import com.learnmanager.dto.request.update.UpdateLearningGoalRequest;
import com.learnmanager.dto.response.LearningGoalResponse;
import com.learnmanager.service.LearningGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learningGoals/")
@RequiredArgsConstructor
public class LearningGoalController {

  private final LearningGoalService learningGoalService;

  @PostMapping("create")
  @ResponseStatus(HttpStatus.CREATED)
  public LearningGoalResponse create(Authentication authentication, @Valid @RequestBody CreateLearningGoalRequest request) {
    return learningGoalService.create(authentication.getName(), request);
  }

  @GetMapping("getAll")
  public List<LearningGoalResponse> getAll(
      Authentication authentication) {
    return learningGoalService.getAll(authentication.getName());
  }

  @GetMapping("getAllByModule/{studyModuleId}")
  public List<LearningGoalResponse> getAllByModule(Authentication authentication, @PathVariable Long studyModuleId) {
    return learningGoalService.getAllByModule(authentication.getName(), studyModuleId);
  }

  @GetMapping("get/{id}")
  public LearningGoalResponse getById(Authentication authentication, @PathVariable Long id) {
    return learningGoalService.getById(authentication.getName(), id);
  }

  @PutMapping("update/{id}")
  public LearningGoalResponse update(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody UpdateLearningGoalRequest request) {
    return learningGoalService.update(authentication.getName(), id, request);
  }

  @DeleteMapping("delete/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(Authentication authentication, @PathVariable Long id) {
    learningGoalService.delete(authentication.getName(), id);
  }
}