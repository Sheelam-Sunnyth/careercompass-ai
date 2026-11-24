package com.careerassistant.resume;

import com.careerassistant.user.UserAccount;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resumes")
public class Resume {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false) private UserAccount user;
    @Column(name = "file_name", nullable = false) private String fileName;
    @Column(name = "file_path", nullable = false) private String filePath;
    @Column(name = "extracted_text", nullable = false, columnDefinition = "LONGTEXT") private String extractedText;
    @Column(name = "rag_indexed", nullable = false) private boolean ragIndexed;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getExtractedText() { return extractedText; }
    public void setExtractedText(String extractedText) { this.extractedText = extractedText; }
    public boolean isRagIndexed() { return ragIndexed; }
    public void setRagIndexed(boolean ragIndexed) { this.ragIndexed = ragIndexed; }
    public Instant getCreatedAt() { return createdAt; }
}
