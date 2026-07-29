package com.learnmanager.repository;

import com.learnmanager.entity.StudyModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudyModuleRepository extends JpaRepository<StudyModule, Long> {

  List<StudyModule> findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(
      String email);

  Optional<StudyModule> findByIdAndUserEmailIgnoreCase(Long id, String email);
}