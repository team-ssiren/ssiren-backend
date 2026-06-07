package com.ssaika.ssiren.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class TokenRefreshRequest {

    @NotBlank(message = "리프레시 토큰은 필수입니다.")
    private final String refreshToken;
}
