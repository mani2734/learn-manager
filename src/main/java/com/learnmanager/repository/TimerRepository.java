package com.learnmanager.repository;

import com.learnmanager.entity.Timer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TimerRepository extends JpaRepository<Timer, Long> {

  void deleteAllByStudyModule_Id(Long studyModuleId);

  void deleteAllByLearningGoal_Id(Long learningGoalId);

  boolean existsByPlannedStudySession_Id(Long plannedStudySessionId);

  Optional<Timer> findByUser_EmailIgnoreCase(String email);

  boolean existsByUser_EmailIgnoreCase(String email);

}
