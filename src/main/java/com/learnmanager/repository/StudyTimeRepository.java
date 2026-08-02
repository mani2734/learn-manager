package com.learnmanager.repository;

import com.learnmanager.entity.StudyTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StudyTimeRepository extends JpaRepository<StudyTime, Long> {

  void deleteAllByStudyModule_Id(Long studyModuleId);

  List<StudyTime> findAllByLearningGoal_Id(Long learningGoalId);

  void deleteAllByLearningGoal_Id(Long learningGoalId);

  boolean existsByPlannedStudySession_Id(Long plannedStudySessionId);

  boolean existsByUser_EmailIgnoreCaseAndStartTimeLessThanAndEndTimeGreaterThan(
      String email,
      LocalDateTime endTime,
      LocalDateTime startTime);

  List<StudyTime> findAllByUser_EmailIgnoreCaseOrderByStartTimeDesc(String email);

  List<StudyTime> findAllByStudyModule_IdOrderByStartTimeDesc(Long studyModuleId);

  List<StudyTime> findAllByLearningGoal_IdOrderByStartTimeDesc(Long learningGoalId);

  Optional<StudyTime> findByIdAndUser_EmailIgnoreCase(Long id, String email);

  boolean existsByUser_EmailIgnoreCaseAndIdNotAndStartTimeLessThanAndEndTimeGreaterThan(
      String email,
      Long excludedStudyTimeId,
      LocalDateTime endTime,
      LocalDateTime startTime);

  List<StudyTime> findAllByUser_EmailIgnoreCaseAndStartTimeLessThanAndEndTimeGreaterThan(
      String email,
      LocalDateTime rangeEnd,
      LocalDateTime rangeStart);

  List<StudyTime> findTop5ByUser_EmailIgnoreCaseOrderByStartTimeDesc(String email);

  Optional<StudyTime> findTopByUser_EmailIgnoreCaseOrderByEndTimeDesc(String email);

  List<StudyTime> findAllByUser_EmailIgnoreCaseAndStudyModule_IdAndStartTimeLessThanAndEndTimeGreaterThan(
      String email,
      Long studyModuleId,
      LocalDateTime rangeEnd,
      LocalDateTime rangeStart);
}
