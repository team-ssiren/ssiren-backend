package com.ssaika.ssiren.domain.chatbot.dto.response;

import com.ssaika.ssiren.domain.chatbot.entity.ChatbotMessage;
import com.ssaika.ssiren.global.enums.ChatbotSenderType;
import java.time.LocalDateTime;

public record ChatbotMessageResponse(
    Long id,
    ChatbotSenderType senderType,
    String message,
    LocalDateTime createdAt
) {

    public static ChatbotMessageResponse from(ChatbotMessage chatbotMessage) {
        return new ChatbotMessageResponse(
            chatbotMessage.getId(),
            chatbotMessage.getSenderType(),
            chatbotMessage.getMessage(),
            chatbotMessage.getCreatedAt()
        );
    }
}
