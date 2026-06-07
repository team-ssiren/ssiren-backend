package com.ssaika.ssiren.domain.notification.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class FcmTokenRequest {

    @NotBlank(message = "FCM 토큰은 필수입니다.")
    @Size(max = 512, message = "FCM 토큰은 512자 이하여야 합니다.")
    private final String fcmToken;
}
