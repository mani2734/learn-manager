package com.learnmanager.repository;

import com.learnmanager.entity.StudyTime;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyTimeRepository extends JpaRepository<StudyTime, Long> {

  void deleteAllByStudyModule_Id(Long studyModuleId);

  List<StudyTime> findAllByLearningGoal_Id(Long learningGoalId);

  void deleteAllByLearningGoal_Id(Long learningGoalId);
}
