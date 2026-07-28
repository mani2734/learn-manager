package com.learnmanager.repository;

import com.learnmanager.entity.LearningGoal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningGoalRepository extends JpaRepository<LearningGoal, Long> {

}
