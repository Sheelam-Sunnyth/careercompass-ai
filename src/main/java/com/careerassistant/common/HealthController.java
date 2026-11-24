package com.careerassistant.common;

import com.careerassistant.config.AppProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class HealthController {
    private final AppProperties properties;
    public HealthController(AppProperties properties) { this.properties = properties; }
    @GetMapping("/api/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "geminiConfigured", properties.getGemini().getApiKey() != null && !properties.getGemini().getApiKey().isBlank());
    }
}
