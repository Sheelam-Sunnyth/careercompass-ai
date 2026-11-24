package com.careerassistant;

import com.careerassistant.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class CareerAssistantApplication {
    public static void main(String[] args) {
        SpringApplication.run(CareerAssistantApplication.class, args);
    }
}
