package com.learnmanager.controller;

import com.learnmanager.dto.CreateStudyTimeRequest;
import com.learnmanager.dto.StudyTimeResponse;
import com.learnmanager.dto.UpdateStudyTimeRequest;
import com.learnmanager.service.StudyTimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

  @GetMapping("getAll")
  public List<StudyTimeResponse> getAll(Authentication authentication) {
    return studyTimeService.getAll(authentication.getName());
  }

  @GetMapping("getAllByStudyModule/{studyModuleId}")
  public List<StudyTimeResponse> getAllByStudyModule(Authentication authentication, @PathVariable Long studyModuleId) {
    return studyTimeService.getAllByStudyModule(authentication.getName(), studyModuleId);
  }

  @GetMapping("getAllByLearningGoal/{learningGoalId}")
  public List<StudyTimeResponse> getAllByLearningGoal(Authentication authentication, @PathVariable Long learningGoalId) {
    return studyTimeService.getAllByLearningGoal(authentication.getName(), learningGoalId);
  }

  @GetMapping("get/{id}")
  public StudyTimeResponse getById(Authentication authentication, @PathVariable Long id) {
    return studyTimeService.getById(authentication.getName(), id);
  }

  @PutMapping("update/{id}")
  public StudyTimeResponse update(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody UpdateStudyTimeRequest request) {
    return studyTimeService.update(authentication.getName(), id, request);
  }

  @DeleteMapping("delete/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(Authentication authentication, @PathVariable Long id) {
    studyTimeService.delete(authentication.getName(), id);
  }
}