package com.learnmanager.repository;

import com.learnmanager.entity.LearningGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningGoalRepository extends JpaRepository<LearningGoal, Long> {

  List<LearningGoal> findAllByStudyModule_Id(
      Long studyModuleId);

  List<LearningGoal> findAllByStudyModule_IdOrderByCreatedAtDesc(
      Long studyModuleId);

  List<LearningGoal> findAllByStudyModule_User_EmailIgnoreCaseOrderByCreatedAtDesc(
      String email);

  Optional<LearningGoal> findByIdAndStudyModule_User_EmailIgnoreCase(Long id, String email);

  void deleteAllByStudyModule_Id(
      Long studyModuleId);
}