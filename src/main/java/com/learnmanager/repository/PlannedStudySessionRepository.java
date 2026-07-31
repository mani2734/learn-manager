package com.learnmanager.repository;

import com.learnmanager.entity.PlannedStudySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlannedStudySessionRepository extends JpaRepository<PlannedStudySession, Long> {

  void deleteAllByStudyModule_Id(
      Long studyModuleId);

  List<PlannedStudySession> findAllByUser_EmailIgnoreCaseOrderByStartTimeAsc(String email);

  List<PlannedStudySession> findAllByStudyModule_IdOrderByStartTimeAsc(Long studyModuleId);

  Optional<PlannedStudySession> findByIdAndUser_EmailIgnoreCase(Long id, String email);

  List<PlannedStudySession> findAllByUser_EmailIgnoreCaseAndStartTimeLessThanAndEndTimeGreaterThan(
      String email,
      LocalDateTime rangeEnd,
      LocalDateTime rangeStart);
}
