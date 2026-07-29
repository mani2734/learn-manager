package com.learnmanager.repository;

import com.learnmanager.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {

  void deleteAllByLearningGoal_StudyModule_Id(Long studyModuleId);

  void deleteAllByLearningGoal_Id(Long learningGoalId);
}
