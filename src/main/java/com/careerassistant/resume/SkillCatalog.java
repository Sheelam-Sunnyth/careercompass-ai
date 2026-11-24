package com.careerassistant.resume;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public final class SkillCatalog {
    private static final String[] SKILLS = {"Java", "Spring Boot", "Spring Security", "Hibernate", "JPA", "REST APIs", "Microservices",
            "SQL", "MySQL", "PostgreSQL", "MongoDB", "Docker", "Kubernetes", "AWS", "Git", "Maven", "JUnit", "Mockito",
            "JavaScript", "HTML", "CSS", "React", "Angular", "Python", "Data Structures", "Algorithms", "Kafka", "Redis", "Jenkins"};
    private SkillCatalog() {}
    public static Set<String> extract(String text) {
        String lower = text.toLowerCase(Locale.ROOT); Set<String> found = new LinkedHashSet<>();
        for (String skill : SKILLS) if (lower.matches("(?s).*\\b" + java.util.regex.Pattern.quote(skill.toLowerCase(Locale.ROOT)) + "\\b.*")) found.add(skill);
        return found;
    }
}
