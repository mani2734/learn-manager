package com.learnmanager.testsupport;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@SpringBootTest
@ActiveProfiles("test")
@Import(FixedClockTestConfig.class)
@Transactional
public abstract class AbstractIntegrationTest {

  @Autowired protected TestDataFactory testDataFactory;

  @PersistenceContext protected EntityManager entityManager;

  @Autowired protected Clock applicationClockTest;

  protected void flushAndClear() {
    entityManager.flush();
    entityManager.clear();
  }
}