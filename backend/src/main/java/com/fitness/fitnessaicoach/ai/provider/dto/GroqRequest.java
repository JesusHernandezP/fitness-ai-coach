package com.fitness.fitnessaicoach.ai.provider.dto;

import java.util.List;

public class GroqRequest {

    private String model;

    private List<GroqMessage> messages;

    public GroqRequest() {
    }

    public GroqRequest(String model, List<GroqMessage> messages) {
        this.model = model;
        this.messages = messages;
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
