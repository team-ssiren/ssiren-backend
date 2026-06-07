package com.ssaika.ssiren.domain.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserConsentUpdateRequest {

    @NotNull(message = "위치 정보 수집 동의 여부는 필수입니다.")
    private final Boolean locationAgreed;

    @NotNull(message = "민감 정보 처리 동의 여부는 필수입니다.")
    private final Boolean sensitiveInfoAgreed;
}
