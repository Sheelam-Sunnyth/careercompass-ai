package com.careerassistant.user;

import jakarta.persistence.*;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id", nullable = false, unique = true) private UserAccount user;
    private String phone;
    private String location;
    private String education;
    @Column(name = "target_role") private String targetRole;
    @Column(name = "career_preferences") private String careerPreferences;
    public Long getId() { return id; }
    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getEducation() { return education; }
    public void setEducation(String education) { this.education = education; }
    public String getTargetRole() { return targetRole; }
    public void setTargetRole(String targetRole) { this.targetRole = targetRole; }
    public String getCareerPreferences() { return careerPreferences; }
    public void setCareerPreferences(String careerPreferences) { this.careerPreferences = careerPreferences; }
}
