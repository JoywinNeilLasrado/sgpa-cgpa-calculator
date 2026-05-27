package com.gradecalculator.repository;

import com.gradecalculator.model.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long> {
    Optional<LoginAttempt> findByAttemptKey(String attemptKey);
    void deleteByAttemptKey(String attemptKey);
}
