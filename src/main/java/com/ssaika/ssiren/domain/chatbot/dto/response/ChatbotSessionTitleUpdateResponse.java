package com.ssaika.ssiren.domain.chatbot.dto.response;

import com.ssaika.ssiren.domain.chatbot.entity.ChatbotSession;

public record ChatbotSessionTitleUpdateResponse(
    String title
) {

    public static ChatbotSessionTitleUpdateResponse from(ChatbotSession chatbotSession) {
        return new ChatbotSessionTitleUpdateResponse(chatbotSession.getTitle());
    }
}
