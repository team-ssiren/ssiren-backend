package com.ssaika.ssiren.domain.chatbot.client.dto;

import java.math.BigDecimal;

public record ChatbotUserLocation(
    BigDecimal lat,
    BigDecimal lng
) {
}
