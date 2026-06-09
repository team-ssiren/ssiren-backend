package com.ssaika.ssiren.domain.chatbot.client.dto;

import java.util.List;

public record ChatbotAnswerRequest(
    String question,
    List<ChatbotHistoryMessage> history,
    ChatbotAnswerContext context
) {
}
