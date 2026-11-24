package com.careerassistant.resume;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "resume_analyses")
public class ResumeAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resume_id", nullable = false) private Resume resume;
    @Column(name = "overall_score", nullable = false) private int overallScore;
    @Column(nullable = false, columnDefinition = "json") private String strengths;
    @Column(nullable = false, columnDefinition = "json") private String weaknesses;
    @Column(nullable = false, columnDefinition = "json") private String recommendations;
    @Column(name = "section_feedback", nullable = false, columnDefinition = "json") private String sectionFeedback;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt = Instant.now();
    public Long getId() { return id; }
    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }
    public int getOverallScore() { return overallScore; }
    public void setOverallScore(int overallScore) { this.overallScore = overallScore; }
    public String getStrengths() { return strengths; }
    public void setStrengths(String strengths) { this.strengths = strengths; }
    public String getWeaknesses() { return weaknesses; }
    public void setWeaknesses(String weaknesses) { this.weaknesses = weaknesses; }
    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }
    public String getSectionFeedback() { return sectionFeedback; }
    public void setSectionFeedback(String sectionFeedback) { this.sectionFeedback = sectionFeedback; }
    public Instant getCreatedAt() { return createdAt; }
}
