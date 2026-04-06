package com.fitness.fitnessaicoach.dto.ai;

public class AIChatMessageResponse {

    private String reply;

    public AIChatMessageResponse() {
    }

    public AIChatMessageResponse(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }
}
