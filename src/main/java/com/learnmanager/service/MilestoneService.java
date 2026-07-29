package com.learnmanager.service;

import com.learnmanager.dto.CreateMilestoneRequest;
import com.learnmanager.dto.MilestoneResponse;
import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.Milestone;
import com.learnmanager.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MilestoneService {

  private final MilestoneRepository milestoneRepository;

  private final HelperService helperService;

  @Transactional
  public MilestoneResponse create(String userEmail, CreateMilestoneRequest request) {
    LearningGoal learningGoal = helperService.findOwnedLearningGoal(userEmail, request.learningGoalId());

    Milestone milestone = new Milestone(learningGoal, request.title().trim(), request.deadline());

    Milestone savedMilestone = milestoneRepository.save(milestone);

    return MilestoneResponse.fromEntity(savedMilestone);
  }
}