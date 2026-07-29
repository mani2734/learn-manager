package com.learnmanager.controller;

import com.learnmanager.dto.CreateMilestoneRequest;
import com.learnmanager.dto.MilestoneResponse;
import com.learnmanager.dto.UpdateMilestoneRequest;
import com.learnmanager.service.MilestoneService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

  @GetMapping("getAll")
  public List<MilestoneResponse> getAll(
      Authentication authentication) {
    return milestoneService.getAll(authentication.getName());
  }

  @GetMapping("getAllByLearningGoal/{learningGoalId}")
  public List<MilestoneResponse> getAllByLearningGoal(Authentication authentication, @PathVariable Long learningGoalId) {
    return milestoneService.getAllByLearningGoal(authentication.getName(), learningGoalId);
  }

  @GetMapping("get/{id}")
  public MilestoneResponse getById(Authentication authentication, @PathVariable Long id) {
    return milestoneService.getById(authentication.getName(), id);
  }

  @PutMapping("update/{id}")
  public MilestoneResponse update(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody UpdateMilestoneRequest request) {
    return milestoneService.update(authentication.getName(), id, request);
  }

  @DeleteMapping("delete/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(Authentication authentication, @PathVariable Long id) {
    milestoneService.delete(authentication.getName(), id);
  }
}