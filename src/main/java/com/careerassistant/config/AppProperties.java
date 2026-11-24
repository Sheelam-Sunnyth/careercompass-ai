package com.careerassistant.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private Jwt jwt = new Jwt();
    private String uploadDir = "./uploads";
    private Gemini gemini = new Gemini();
    private Chroma chroma = new Chroma();

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    public String getUploadDir() { return uploadDir; }
    public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
    public Gemini getGemini() { return gemini; }
    public void setGemini(Gemini gemini) { this.gemini = gemini; }
    public Chroma getChroma() { return chroma; }
    public void setChroma(Chroma chroma) { this.chroma = chroma; }

    public static class Jwt {
        private String secret;
        private long expirationMinutes = 480;
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getExpirationMinutes() { return expirationMinutes; }
        public void setExpirationMinutes(long expirationMinutes) { this.expirationMinutes = expirationMinutes; }
    }
    public static class Gemini {
        private String apiKey;
        private String model = "gemini-2.5-flash-lite";
        private String embeddingModel = "gemini-embedding-2";
        private String baseUrl;
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public String getEmbeddingModel() { return embeddingModel; }
        public void setEmbeddingModel(String embeddingModel) { this.embeddingModel = embeddingModel; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }
    public static class Chroma {
        private String baseUrl;
        private String tenant = "default_tenant";
        private String database = "default_database";
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getTenant() { return tenant; }
        public void setTenant(String tenant) { this.tenant = tenant; }
        public String getDatabase() { return database; }
        public void setDatabase(String database) { this.database = database; }
    }
}
