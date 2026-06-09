package com.ssaika.ssiren.domain.chatbot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatbotSessionTitleUpdateRequest(
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 10, message = "제목은 최대 10자까지 입력할 수 있습니다.")
    String title
) {
}
