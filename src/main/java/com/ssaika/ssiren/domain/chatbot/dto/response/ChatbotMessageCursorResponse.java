package com.ssaika.ssiren.domain.chatbot.dto.response;

import java.util.List;

public record ChatbotMessageCursorResponse(
    List<ChatbotMessageResponse> content,
    Long nextCursor,
    Boolean hasNext
) {

    public static ChatbotMessageCursorResponse of(List<ChatbotMessageResponse> content,
        Boolean hasNext) {
        Long nextCursor = content.isEmpty()
            ? null
            : content.get(content.size() - 1).id();

        return new ChatbotMessageCursorResponse(content, nextCursor, hasNext);
    }
}
