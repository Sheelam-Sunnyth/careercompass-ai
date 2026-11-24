package com.careerassistant.ai;

import com.careerassistant.common.ApiException;
import com.careerassistant.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {
    private final AppProperties properties;
    private final ObjectMapper mapper;
    private final RestClient client;
    public GeminiClient(AppProperties properties, ObjectMapper mapper) {
        this.properties = properties; this.mapper = mapper;
        this.client = RestClient.builder().baseUrl(properties.getGemini().getBaseUrl()).build();
    }
    public boolean available() {
        return properties.getGemini().getApiKey() != null && !properties.getGemini().getApiKey().isBlank();
    }
    public JsonNode generateJson(String prompt) {
        requireConfigured();
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json", "temperature", 0.2));
        String text = requestText(properties.getGemini().getModel(), "generateContent", body);
        try { return mapper.readTree(text); }
        catch (Exception ex) { throw ApiException.serviceUnavailable("Gemini returned an invalid structured response. Please retry."); }
    }
    public String generateText(String prompt) {
        requireConfigured();
        return requestText(properties.getGemini().getModel(), "generateContent",
                Map.of("contents", List.of(Map.of("role", "user", "parts", List.of(Map.of("text", prompt)))),
                        "generationConfig", Map.of("temperature", 0.3)));
    }
    public List<Double> embed(String text) {
        requireConfigured();
        Map<String, Object> body = Map.of("model", "models/" + properties.getGemini().getEmbeddingModel(),
                "content", Map.of("parts", List.of(Map.of("text", text))));
        try {
            JsonNode response = client.post().uri("/models/{model}:embedContent", properties.getGemini().getEmbeddingModel())
                    .header("x-goog-api-key", properties.getGemini().getApiKey()).contentType(MediaType.APPLICATION_JSON)
                    .body(body).retrieve().body(JsonNode.class);
            JsonNode values = response.path("embedding").path("values");
            if (!values.isArray() || values.isEmpty()) throw ApiException.serviceUnavailable("Gemini embedding response was empty.");
            java.util.ArrayList<Double> vector = new java.util.ArrayList<>();
            values.forEach(value -> vector.add(value.asDouble()));
            return vector;
        } catch (RestClientResponseException ex) {
            throw ApiException.serviceUnavailable("Gemini embeddings are unavailable right now. Check your API key, model access, and quota.");
        }
    }
    private String requestText(String model, String action, Map<String, Object> body) {
        try {
            JsonNode response = client.post().uri("/models/{model}:{action}", model, action)
                    .header("x-goog-api-key", properties.getGemini().getApiKey()).contentType(MediaType.APPLICATION_JSON)
                    .body(body).retrieve().body(JsonNode.class);
            String text = response.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
            if (text.isBlank()) throw ApiException.serviceUnavailable("Gemini did not return usable content. Please retry.");
            return text;
        } catch (RestClientResponseException ex) {
            throw ApiException.serviceUnavailable("Gemini is unavailable right now. Check your API key, model access, and quota.");
        }
    }
    private void requireConfigured() {
        if (!available()) throw ApiException.serviceUnavailable("AI features require GEMINI_API_KEY. Add it to your environment and restart the application.");
    }
}
