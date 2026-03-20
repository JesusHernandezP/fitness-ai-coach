package com.fitness.fitnessaicoach.ai.provider.groq;

import com.fitness.fitnessaicoach.ai.provider.dto.GroqRequest;
import com.fitness.fitnessaicoach.ai.provider.dto.GroqResponse;
import com.fitness.fitnessaicoach.config.GroqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GroqClient {

    private final RestTemplate groqRestTemplate;
    private final GroqConfig groqConfig;

    public record ConnectionStatus(boolean available, int statusCode, String message) {
    }

    public String getCoachingResponse(String prompt) {
        if (!groqConfig.hasApiKey()) {
            throw new IllegalStateException("Groq API key is not configured.");
        }

        GroqRequest request = buildRequest(prompt, null);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(groqConfig.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GroqRequest> entity = new HttpEntity<>(request, headers);

        try {
            GroqResponse response = groqRestTemplate.postForObject(
                    groqConfig.getApiUrl(),
                    entity,
                    GroqResponse.class
            );

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new IllegalStateException("No choices were returned from Groq.");
            }

            GroqResponse.GroqChoice firstChoice = response.getChoices().get(0);
            if (firstChoice.getMessage() == null || firstChoice.getMessage().getContent() == null) {
                throw new IllegalStateException("Groq returned an empty assistant message.");
            }

            return firstChoice.getMessage().getContent().trim();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            String detail = ex.getResponseBodyAsString();
            throw new IllegalStateException("Groq request failed with status " + ex.getStatusCode() + ". " + detail, ex);
        } catch (RestClientException ex) {
            throw new IllegalStateException("Error communicating with Groq API.", ex);
        }
    }

    public ConnectionStatus checkConnectivity() {
        if (!groqConfig.hasApiKey()) {
            return new ConnectionStatus(false, 401, "Groq API key is not configured.");
        }

        GroqRequest request = buildRequest("Health check for Fitness AI Coach", 1);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(groqConfig.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<GroqRequest> entity = new HttpEntity<>(request, headers);

        try {
            GroqResponse response = groqRestTemplate.postForObject(
                    groqConfig.getApiUrl(),
                    entity,
                    GroqResponse.class
            );

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                return new ConnectionStatus(false, 502, "Groq API returned an empty response.");
            }

            return new ConnectionStatus(true, 200, "Groq API is reachable.");
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return new ConnectionStatus(false, ex.getStatusCode().value(), ex.getResponseBodyAsString());
        } catch (RestClientException ex) {
            return new ConnectionStatus(false, 503, "Error communicating with Groq API.");
        }
    }

    private GroqRequest buildRequest(String prompt, Integer maxTokens) {
        return new GroqRequest(
                groqConfig.getModel(),
                List.of(new GroqRequest.GroqMessage("user", prompt)),
                maxTokens
        );
    }
}
