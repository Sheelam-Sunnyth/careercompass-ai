package com.careerassistant.resume;

import jakarta.persistence.*;

@Entity
@Table(name = "resume_skills")
public class ResumeSkill {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "resume_id", nullable = false) private Resume resume;
    @Column(name = "skill_name", nullable = false) private String skillName;
    @Column(nullable = false) private String proficiency = "DEMONSTRATED";
    public Resume getResume() { return resume; }
    public void setResume(Resume resume) { this.resume = resume; }
    public String getSkillName() { return skillName; }
    public void setSkillName(String skillName) { this.skillName = skillName; }
    public String getProficiency() { return proficiency; }
    public void setProficiency(String proficiency) { this.proficiency = proficiency; }
}
