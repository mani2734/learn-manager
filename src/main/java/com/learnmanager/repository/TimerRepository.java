package com.learnmanager.repository;

import com.learnmanager.entity.Timer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimerRepository extends JpaRepository<Timer, Long> {

  void deleteAllByStudyModule_Id(Long studyModuleId);
}
