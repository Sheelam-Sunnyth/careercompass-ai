package com.careerassistant.job;
import com.careerassistant.user.UserAccount;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name = "job_descriptions")
public class JobDescription {
 @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="user_id",nullable=false) private UserAccount user;
 @Column(nullable=false) private String company;
 @Column(name="job_title",nullable=false) private String jobTitle;
 @Column(nullable=false,columnDefinition="TEXT") private String description;
 @Column(name="created_at",nullable=false,updatable=false) private Instant createdAt=Instant.now();
 public Long getId(){return id;} public UserAccount getUser(){return user;} public void setUser(UserAccount v){user=v;}
 public String getCompany(){return company;} public void setCompany(String v){company=v;}
 public String getJobTitle(){return jobTitle;} public void setJobTitle(String v){jobTitle=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public Instant getCreatedAt(){return createdAt;}
}
