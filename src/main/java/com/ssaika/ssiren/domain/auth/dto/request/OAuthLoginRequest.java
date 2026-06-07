package com.ssaika.ssiren.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class OAuthLoginRequest {

    @NotBlank(message = "소셜 로그인 제공자는 필수입니다.")
    private final String provider;

    @NotBlank(message = "소셜 로그인 토큰은 필수입니다.")
    private final String providerToken;
}
