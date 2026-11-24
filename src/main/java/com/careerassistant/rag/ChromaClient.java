package com.careerassistant.rag;

import com.careerassistant.common.ApiException;
import com.careerassistant.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import java.util.List;
import java.util.Map;

@Service
public class ChromaClient {
    private static final String COLLECTION = "career_documents";
    private final AppProperties.Chroma config;
    private final RestClient client;
    private volatile String collectionId;
    public ChromaClient(AppProperties properties) {
        this.config = properties.getChroma();
        this.client = RestClient.builder().baseUrl(config.getBaseUrl()).build();
    }
    public void add(String vectorId, List<Double> embedding, String document, Long userId, Long resumeId) {
        String id = collectionId();
        try {
            client.post().uri("/api/v2/tenants/{tenant}/databases/{database}/collections/{id}/add", config.getTenant(), config.getDatabase(), id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("ids", List.of(vectorId), "embeddings", List.of(embedding), "documents", List.of(document),
                            "metadatas", List.of(Map.of("userId", userId, "resumeId", resumeId))))
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException ex) {
            throw ApiException.serviceUnavailable("ChromaDB could not store the document embedding. Confirm the Chroma service is running.");
        }
    }
    public List<RetrievedChunk> query(List<Double> embedding, Long userId, int limit) {
        String id = collectionId();
        try {
            JsonNode response = client.post().uri("/api/v2/tenants/{tenant}/databases/{database}/collections/{id}/query", config.getTenant(), config.getDatabase(), id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("query_embeddings", List.of(embedding), "n_results", limit,
                            "where", Map.of("userId", Map.of("$eq", userId)),
                            "include", List.of("documents", "metadatas", "distances")))
                    .retrieve().body(JsonNode.class);
            JsonNode docs = response.path("documents").path(0), metadata = response.path("metadatas").path(0), distances = response.path("distances").path(0);
            java.util.ArrayList<RetrievedChunk> results = new java.util.ArrayList<>();
            for (int i = 0; i < docs.size(); i++) {
                results.add(new RetrievedChunk(docs.path(i).asText(), metadata.path(i).path("resumeId").asLong(), distances.path(i).asDouble()));
            }
            return results;
        } catch (RestClientResponseException ex) {
            throw ApiException.serviceUnavailable("ChromaDB could not retrieve career-document context. Confirm the Chroma service is running.");
        }
    }
    public void delete(List<String> vectorIds) {
        if (vectorIds.isEmpty()) return;
        try {
            client.post().uri("/api/v2/tenants/{tenant}/databases/{database}/collections/{id}/delete", config.getTenant(), config.getDatabase(), collectionId())
                    .contentType(MediaType.APPLICATION_JSON).body(Map.of("ids", vectorIds)).retrieve().toBodilessEntity();
        } catch (RuntimeException ignored) { }
    }
    private String collectionId() {
        if (collectionId != null) return collectionId;
        synchronized (this) {
            if (collectionId != null) return collectionId;
            String base = "/api/v2/tenants/" + config.getTenant() + "/databases/" + config.getDatabase() + "/collections";
            try {
                JsonNode found = client.get().uri(base + "/{name}", COLLECTION).retrieve().body(JsonNode.class);
                collectionId = found.path("id").asText();
            } catch (RestClientResponseException missing) {
                try {
                    JsonNode created = client.post().uri(base).contentType(MediaType.APPLICATION_JSON)
                            .body(Map.of("name", COLLECTION)).retrieve().body(JsonNode.class);
                    collectionId = created.path("id").asText();
                } catch (RestClientResponseException ex) {
                    throw ApiException.serviceUnavailable("ChromaDB is unavailable. Start the Chroma service before using document search.");
                }
            }
            if (collectionId == null || collectionId.isBlank()) throw ApiException.serviceUnavailable("ChromaDB did not return a collection identifier.");
            return collectionId;
        }
    }
    public record RetrievedChunk(String text, Long resumeId, double distance) {}
}
