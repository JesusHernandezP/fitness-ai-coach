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

    public String getCoachingResponse(String prompt) {
        if (!groqConfig.hasApiKey()) {
            throw new IllegalStateException("Groq API key is not configured.");
        }

        GroqRequest request = new GroqRequest(
                groqConfig.getModel(),
                List.of(new GroqRequest.GroqMessage("user", prompt))
        );

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
}
