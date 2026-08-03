package com.learnmanager.repository;

import com.learnmanager.entity.LearningGoal;
import com.learnmanager.entity.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
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

  List<LearningGoal> findAllByStudyModule_User_EmailIgnoreCaseAndStatusNotInAndDeadlineBetweenOrderByDeadlineAsc(
      String email, Collection<GoalStatus> excludedStatuses,
      LocalDate deadlineStart,
      LocalDate deadlineEnd);
}