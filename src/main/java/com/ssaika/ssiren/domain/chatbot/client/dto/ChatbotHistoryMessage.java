package com.ssaika.ssiren.domain.chatbot.client.dto;

import com.ssaika.ssiren.domain.chatbot.entity.ChatbotMessage;
import com.ssaika.ssiren.global.enums.ChatbotSenderType;

public record ChatbotHistoryMessage(
    String role,
    String content
) {

    public static ChatbotHistoryMessage from(ChatbotMessage chatbotMessage) {
        return new ChatbotHistoryMessage(
            chatbotMessage.getSenderType() == ChatbotSenderType.USER ? "user" : "assistant",
            chatbotMessage.getMessage()
        );
    }
}
