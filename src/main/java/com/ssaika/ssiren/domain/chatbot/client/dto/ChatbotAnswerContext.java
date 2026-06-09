package com.ssaika.ssiren.domain.chatbot.client.dto;

import java.util.List;

public record ChatbotAnswerContext(
    String scope,
    List<ChatbotReportContext> reports,
    ChatbotUserLocation userLocation
) {
}
