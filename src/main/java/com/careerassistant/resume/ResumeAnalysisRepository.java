package com.careerassistant.resume;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ResumeAnalysisRepository extends JpaRepository<ResumeAnalysis, Long> {
    List<ResumeAnalysis> findByResumeIdOrderByCreatedAtDesc(Long resumeId);
    Optional<ResumeAnalysis> findByIdAndResumeId(Long id, Long resumeId);
}
