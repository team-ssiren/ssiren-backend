package com.ssaika.ssiren.domain.chatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ChatbotMessageSendRequest(
    @NotBlank String message,
    @NotNull BigDecimal latitude,
    @NotNull BigDecimal longitude
) {
}
