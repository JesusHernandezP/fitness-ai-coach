package com.fitness.fitnessaicoach.ai.provider.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class GroqRequest {

    private String model;

    private List<GroqMessage> messages;

    @JsonProperty("max_tokens")
    private Integer maxTokens;

    public GroqRequest() {
    }

    public GroqRequest(String model, List<GroqMessage> messages) {
        this.model = model;
        this.messages = messages;
    }

    public GroqRequest(String model, List<GroqMessage> messages, Integer maxTokens) {
        this.model = model;
        this.messages = messages;
        this.maxTokens = maxTokens;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public List<GroqMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<GroqMessage> messages) {
        this.messages = messages;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public static class GroqMessage {

        private String role;

        private String content;

        public GroqMessage() {
        }

        public GroqMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
