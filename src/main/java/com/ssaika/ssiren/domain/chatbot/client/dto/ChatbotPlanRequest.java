package com.ssaika.ssiren.domain.chatbot.client.dto;

import java.util.List;

public record ChatbotPlanRequest(
    String question,
    List<ChatbotHistoryMessage> history
) {
}
