package com.fitness.fitnessaicoach.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class GroqConfig {

    @Value("${groq.api.key:}")
    private String apiKey;

    @Value("${groq.api.url:https://api.groq.com/openai/v1/chat/completions}")
    private String apiUrl;

    @Value("${groq.api.model:llama3-70b-8192}")
    private String model;

    @Bean
    public RestTemplate groqRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5_000);
        requestFactory.setReadTimeout(15_000);
        return new RestTemplate(requestFactory);
    }

    public String getApiKey() {
        return decodeIfEncrypted(apiKey);
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getModel() {
        return model;
    }

    public boolean hasApiKey() {
        String key = getApiKey();
        return key != null && !key.isBlank();
    }

    private String decodeIfEncrypted(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }

        String trimmed = value.trim();
        if (trimmed.startsWith("ENC(") && trimmed.endsWith(")")) {
            String encoded = trimmed.substring(4, trimmed.length() - 1);
            return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
        }
        return value;
    }
}
