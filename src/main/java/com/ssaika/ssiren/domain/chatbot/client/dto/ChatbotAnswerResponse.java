package com.ssaika.ssiren.domain.chatbot.client.dto;

import java.util.List;

public record ChatbotAnswerResponse(
    String answer,
    List<Long> usedReportIds
) {
}
