package com.learnmanager.repository;

import com.learnmanager.entity.StudyTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTimeRepository extends JpaRepository<StudyTime, Long> {

  void deleteAllByStudyModule_Id(Long studyModuleId);
}
