package com.careerassistant.tracker;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {
    List<JobApplication> findByUserIdOrderByApplicationDateDesc(Long userId);
    Optional<JobApplication> findByIdAndUserId(Long id, Long userId);
    long countByUserIdAndStatus(Long userId, ApplicationStatus status);
}
