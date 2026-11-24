package com.careerassistant.job;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface JobMatchRepository extends JpaRepository<JobMatch,Long>{ Optional<JobMatch> findByIdAndJobUserId(Long id,Long userId); }
