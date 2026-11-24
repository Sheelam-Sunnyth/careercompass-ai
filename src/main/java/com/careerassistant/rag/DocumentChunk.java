package com.careerassistant.rag;

import com.careerassistant.resume.Resume;
import jakarta.persistence.*;

@Entity
@Table(name = "document_chunks")
public class DocumentChunk {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resume_id", nullable = false) private Resume resume;
    @Column(name = "chunk_index", nullable = false) private int chunkIndex;
    @Column(name = "chunk_text", nullable = false) private String chunkText;
    @Column(name = "vector_id", nullable = false, unique = true) private String vectorId;
    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }
    public int getChunkIndex() { return chunkIndex; }
    public void setChunkIndex(int chunkIndex) { this.chunkIndex = chunkIndex; }
    public String getChunkText() { return chunkText; }
    public void setChunkText(String chunkText) { this.chunkText = chunkText; }
    public String getVectorId() { return vectorId; }
    public void setVectorId(String vectorId) { this.vectorId = vectorId; }
}
