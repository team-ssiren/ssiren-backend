package com.ssaika.ssiren.domain.chatbot.dto.response;

import java.util.List;

public record ChatbotMessageSendResponse(
    String answer,
    List<Long> usedReportIds
) {

    public static ChatbotMessageSendResponse of(String answer, List<Long> usedReportIds) {
        return new ChatbotMessageSendResponse(answer, usedReportIds);
    }
}
