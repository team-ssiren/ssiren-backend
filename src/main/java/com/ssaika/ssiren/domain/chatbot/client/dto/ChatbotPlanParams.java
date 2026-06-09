package com.ssaika.ssiren.domain.chatbot.client.dto;

public record ChatbotPlanParams(
    String categoryCode,
    Integer radiusMeters
) {

    public Integer resolveRadiusMeters() {
        return radiusMeters == null ? 500 : radiusMeters;
    }
}
