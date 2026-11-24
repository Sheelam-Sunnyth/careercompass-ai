package com.careerassistant.resume;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
class SkillCatalogTest {
 @Test void extractsOnlyPresentSkills() {
  assertEquals(Set.of("Java","Spring Boot","REST APIs","MySQL","Docker","JUnit"),
   SkillCatalog.extract("Built REST APIs with Java, Spring Boot, MySQL, Docker and JUnit."));
 }
}
