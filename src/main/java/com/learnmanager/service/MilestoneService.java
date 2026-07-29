package com.learnmanager.service;

import com.learnmanager.dto.CreateMilestoneRequest;
import com.learnmanager.dto.MilestoneResponse;
import com.learnmanager.dto.UpdateMilestoneRequest;
import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.Milestone;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.MilestoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

  @Transactional(readOnly = true)
  public List<MilestoneResponse> getAll(String userEmail) {
    return milestoneRepository.findAllByLearningGoal_StudyModule_User_EmailIgnoreCaseOrderByCreatedAtDesc(helperService.normalizeEmail(
        userEmail)).stream().map(MilestoneResponse::fromEntity).toList();
  }

  @Transactional(readOnly = true)
  public List<MilestoneResponse> getAllByLearningGoal(String userEmail, Long learningGoalId) {
    LearningGoal learningGoal = helperService.findOwnedLearningGoal(userEmail, learningGoalId);

    return milestoneRepository.findAllByLearningGoal_IdOrderByCreatedAtDesc(learningGoal.getId())
                              .stream()
                              .map(MilestoneResponse::fromEntity)
                              .toList();
  }

  @Transactional(readOnly = true)
  public MilestoneResponse getById(String userEmail, Long milestoneId) {
    Milestone milestone = findOwnedMilestone(userEmail, milestoneId);

    return MilestoneResponse.fromEntity(milestone);
  }

  @Transactional
  public MilestoneResponse update(String userEmail, Long milestoneId, UpdateMilestoneRequest request) {
    Milestone milestone = findOwnedMilestone(userEmail, milestoneId);

    milestone.setTitle(request.title().trim());
    milestone.setDeadline(request.deadline());
    milestone.setStatus(request.status());

    Milestone updatedMilestone = milestoneRepository.save(milestone);

    return MilestoneResponse.fromEntity(updatedMilestone);
  }

  @Transactional
  public void delete(String userEmail, Long milestoneId) {
    Milestone milestone = findOwnedMilestone(userEmail, milestoneId);

    milestoneRepository.delete(milestone);
  }

  private Milestone findOwnedMilestone(String userEmail, Long milestoneId) {
    return milestoneRepository.findByIdAndLearningGoal_StudyModule_User_EmailIgnoreCase(
                                  milestoneId,
                                  helperService.normalizeEmail(userEmail))
                              .orElseThrow(() -> new ResourceNotFoundException("Milestone not found"));
  }

}