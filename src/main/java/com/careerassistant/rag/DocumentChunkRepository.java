package com.careerassistant.rag;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByResumeId(Long resumeId);
    void deleteByResumeId(Long resumeId);
}
