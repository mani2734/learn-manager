package com.learnmanager.repository;

import com.learnmanager.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

  void deleteAllByLearningGoal_StudyModule_Id(Long studyModuleId);

  void deleteAllByLearningGoal_Id(Long learningGoalId);

  List<Milestone> findAllByLearningGoal_StudyModule_User_EmailIgnoreCaseOrderByCreatedAtDesc(
      String email);

  List<Milestone> findAllByLearningGoal_IdOrderByCreatedAtDesc(
      Long learningGoalId);

  Optional<Milestone> findByIdAndLearningGoal_StudyModule_User_EmailIgnoreCase(Long id, String email);

  List<Milestone> findAllByLearningGoal_StudyModule_User_EmailIgnoreCase(String email);
}
