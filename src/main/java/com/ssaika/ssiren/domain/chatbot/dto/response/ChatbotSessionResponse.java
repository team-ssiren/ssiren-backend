package com.ssaika.ssiren.domain.chatbot.dto.response;

import com.ssaika.ssiren.domain.chatbot.entity.ChatbotSession;
import java.time.LocalDateTime;

public record ChatbotSessionResponse(
    Long id,
    String title,
    LocalDateTime createdAt
) {

    public static ChatbotSessionResponse from(ChatbotSession chatbotSession) {
        return new ChatbotSessionResponse(
            chatbotSession.getId(),
            chatbotSession.getTitle(),
            chatbotSession.getCreatedAt()
        );
    }
}
