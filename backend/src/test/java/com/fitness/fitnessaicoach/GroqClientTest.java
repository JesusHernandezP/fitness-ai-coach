package com.fitness.fitnessaicoach;

import com.fitness.fitnessaicoach.ai.provider.groq.GroqClient;
import com.fitness.fitnessaicoach.ai.provider.dto.GroqResponse;
import com.fitness.fitnessaicoach.config.GroqConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroqClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private GroqConfig groqConfig;

    @InjectMocks
    private GroqClient groqClient;

    @Test
    void checkConnectivityReturnsAvailableWhenGroqReturnsAnswer() {
        when(groqConfig.hasApiKey()).thenReturn(true);
        when(groqConfig.getApiKey()).thenReturn("test-key");
        when(groqConfig.getApiUrl()).thenReturn("https://api.groq.com/openai/v1/chat/completions");
        when(groqConfig.getModel()).thenReturn("llama3-70b-8192");

        GroqResponse response = new GroqResponse();
        GroqResponse.GroqChoiceMessage message = new GroqResponse.GroqChoiceMessage();
        message.setContent("ok");
        GroqResponse.GroqChoice choice = new GroqResponse.GroqChoice();
        choice.setMessage(message);
        response.setChoices(List.of(choice));

        when(restTemplate.postForObject(anyString(), any(), eq(GroqResponse.class))).thenReturn(response);

        GroqClient.ConnectionStatus status = groqClient.checkConnectivity();

        assertThat(status.available()).isTrue();
        assertThat(status.statusCode()).isEqualTo(200);
        assertThat(status.message()).isEqualTo("Groq API is reachable.");
    }

    @Test
    void checkConnectivityReturnsUnavailableWhenApiKeyMissing() {
        when(groqConfig.hasApiKey()).thenReturn(false);

        GroqClient.ConnectionStatus status = groqClient.checkConnectivity();

        assertThat(status.available()).isFalse();
        assertThat(status.statusCode()).isEqualTo(401);
        assertThat(status.message()).isEqualTo("Groq API key is not configured.");
    }

    @Test
    void checkConnectivityReturnsUnavailableWhenGroqReturnsError() {
        when(groqConfig.hasApiKey()).thenReturn(true);
        when(groqConfig.getApiKey()).thenReturn("test-key");
        when(groqConfig.getApiUrl()).thenReturn("https://api.groq.com/openai/v1/chat/completions");
        when(groqConfig.getModel()).thenReturn("llama3-70b-8192");

        HttpClientErrorException unauthorized = HttpClientErrorException.create(
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                HttpHeaders.EMPTY,
                "Unauthorized".getBytes(StandardCharsets.UTF_8),
                null
        );
        when(restTemplate.postForObject(anyString(), any(), eq(GroqResponse.class))).thenThrow(unauthorized);

        GroqClient.ConnectionStatus status = groqClient.checkConnectivity();

        assertThat(status.available()).isFalse();
        assertThat(status.statusCode()).isEqualTo(401);
        assertThat(status.message()).isEqualTo("Unauthorized");
    }

    @Test
    void checkConnectivityReturnsUnavailableOnNetworkError() {
        when(groqConfig.hasApiKey()).thenReturn(true);
        when(groqConfig.getApiKey()).thenReturn("test-key");
        when(groqConfig.getApiUrl()).thenReturn("https://api.groq.com/openai/v1/chat/completions");
        when(groqConfig.getModel()).thenReturn("llama3-70b-8192");
        when(restTemplate.postForObject(anyString(), any(), eq(GroqResponse.class)))
                .thenThrow(new RestClientException("timeout"));

        GroqClient.ConnectionStatus status = groqClient.checkConnectivity();

        assertThat(status.available()).isFalse();
        assertThat(status.statusCode()).isEqualTo(503);
        assertThat(status.message()).isEqualTo("Error communicating with Groq API.");
    }
}

