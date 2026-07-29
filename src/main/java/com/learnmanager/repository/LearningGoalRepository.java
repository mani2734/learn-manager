package com.learnmanager.repository;

import com.learnmanager.entity.LearningGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningGoalRepository extends JpaRepository<LearningGoal, Long> {

  List<LearningGoal> findAllByStudyModule_Id(
      Long studyModuleId);
}