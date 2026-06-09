package com.ssaika.ssiren.domain.chatbot.dto.response;

import java.util.List;

public record ChatbotMessageSendResponse(
    ChatbotSessionResponse session,
    List<ChatbotMessageResponse> messages
) {

    public static ChatbotMessageSendResponse of(
        ChatbotSessionResponse session,
        List<ChatbotMessageResponse> messages) {
        return new ChatbotMessageSendResponse(session, messages);
    }
}
