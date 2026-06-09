package com.ssaika.ssiren.domain.chatbot.client.dto;

public record ChatbotPlanResponse(
    ChatbotAction action,
    ChatbotPlanParams params,
    String answer
) {
}
