package com.careerassistant.job;
import com.careerassistant.resume.Resume;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="job_matches")
public class JobMatch {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="resume_id",nullable=false) private Resume resume;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="job_id",nullable=false) private JobDescription job;
 @Column(name="match_score",nullable=false) private int matchScore;
 @Column(name="matched_skills",nullable=false,columnDefinition="json") private String matchedSkills;
 @Column(name="missing_skills",nullable=false,columnDefinition="json") private String missingSkills;
 @Column(name="partial_skills",nullable=false,columnDefinition="json") private String partialSkills;
 @Column(nullable=false,columnDefinition="TEXT") private String report;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt=Instant.now();
 public Long getId(){return id;} public Resume getResume(){return resume;} public void setResume(Resume v){resume=v;}
 public JobDescription getJob(){return job;} public void setJob(JobDescription v){job=v;} public int getMatchScore(){return matchScore;} public void setMatchScore(int v){matchScore=v;}
 public String getMatchedSkills(){return matchedSkills;} public void setMatchedSkills(String v){matchedSkills=v;} public String getMissingSkills(){return missingSkills;} public void setMissingSkills(String v){missingSkills=v;}
 public String getPartialSkills(){return partialSkills;} public void setPartialSkills(String v){partialSkills=v;} public String getReport(){return report;} public void setReport(String v){report=v;} public Instant getCreatedAt(){return createdAt;}
}
