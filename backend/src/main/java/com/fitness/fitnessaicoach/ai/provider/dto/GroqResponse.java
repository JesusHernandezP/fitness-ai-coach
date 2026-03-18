package com.fitness.fitnessaicoach.ai.provider.dto;

import java.util.List;

public class GroqResponse {

    private String id;

    private String object;

    private List<GroqChoice> choices;

    private GroqUsage usage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public List<GroqChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<GroqChoice> choices) {
        this.choices = choices;
    }

    public GroqUsage getUsage() {
        return usage;
    }

    public void setUsage(GroqUsage usage) {
        this.usage = usage;
    }

    public static class GroqChoice {

        private Integer index;

        private GroqChoiceMessage message;

        private String finishReason;

        public Integer getIndex() {
            return index;
        }

        public void setIndex(Integer index) {
            this.index = index;
        }

        public GroqChoiceMessage getMessage() {
            return message;
        }

        public void setMessage(GroqChoiceMessage message) {
            this.message = message;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }

    public static class GroqChoiceMessage {

        private String role;

        private String content;

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

    public static class GroqUsage {

        private Integer promptTokens;

        private Integer completionTokens;

        private Integer totalTokens;

        public Integer getPromptTokens() {
            return promptTokens;
        }

        public void setPromptTokens(Integer promptTokens) {
            this.promptTokens = promptTokens;
        }

        public Integer getCompletionTokens() {
            return completionTokens;
        }

        public void setCompletionTokens(Integer completionTokens) {
            this.completionTokens = completionTokens;
        }

        public Integer getTotalTokens() {
            return totalTokens;
        }

        public void setTotalTokens(Integer totalTokens) {
            this.totalTokens = totalTokens;
        }
    }
}
