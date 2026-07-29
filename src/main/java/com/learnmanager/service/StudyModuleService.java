package com.learnmanager.service;

import com.learnmanager.dto.CreateStudyModuleRequest;
import com.learnmanager.dto.StudyModuleResponse;
import com.learnmanager.entity.StudyModule;
import com.learnmanager.entity.User;
import com.learnmanager.exception.ResourceNotFoundException;
import com.learnmanager.repository.StudyModuleRepository;
import com.learnmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyModuleService {

  private static final BigDecimal HOURS_PER_ECTS = BigDecimal.valueOf(30);

  private final StudyModuleRepository studyModuleRepository;

  private final UserRepository userRepository;

  @Transactional
  public StudyModuleResponse create(String userEmail, CreateStudyModuleRequest request) {
    User user = userRepository.findByEmailIgnoreCase(userEmail.trim())
                              .orElseThrow(() -> new UsernameNotFoundException("Authenticated user no longer exists"));

    BigDecimal workloadHours = calculateWorkloadHours(request);

    StudyModule studyModule = new StudyModule(
        user,
        request.name().trim(),
        normalizeOptionalText(request.code()),
        normalizeOptionalText(request.description()),
        request.ects(),
        workloadHours);

    StudyModule savedStudyModule = studyModuleRepository.save(studyModule);

    return StudyModuleResponse.fromEntity(savedStudyModule);
  }

  @Transactional(readOnly = true)
  public List<StudyModuleResponse> getAll(String userEmail) {
    return studyModuleRepository.findAllByUserEmailIgnoreCaseOrderByCreatedAtDesc(userEmail.trim())
                                .stream()
                                .map(StudyModuleResponse::fromEntity)
                                .toList();
  }

  @Transactional(readOnly = true)
  public StudyModuleResponse getById(String userEmail, Long moduleId) {
    StudyModule studyModule = findOwnedModule(userEmail, moduleId);

    return StudyModuleResponse.fromEntity(studyModule);
  }

  private StudyModule findOwnedModule(String userEmail, Long moduleId) {
    return studyModuleRepository.findByIdAndUserEmailIgnoreCase(moduleId, userEmail.trim())
                                .orElseThrow(() -> new ResourceNotFoundException("Study module not found"));
  }

  private BigDecimal calculateWorkloadHours(CreateStudyModuleRequest request) {
    if (request.workloadHours() != null) {
      return request.workloadHours();
    }

    return BigDecimal.valueOf(request.ects()).multiply(HOURS_PER_ECTS);
  }

  private String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }
}