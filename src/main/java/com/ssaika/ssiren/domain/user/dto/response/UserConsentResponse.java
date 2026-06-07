package com.ssaika.ssiren.domain.user.dto.response;

import com.ssaika.ssiren.domain.user.entity.UserConsent;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserConsentResponse {

    private final Long id;
    private final Boolean locationAgreed;
    private final Boolean sensitiveInfoAgreed;
    private final LocalDateTime sensitiveInfoAgreedAt;
    private final LocalDateTime updatedAt;

    public static UserConsentResponse from(UserConsent userConsent) {
        return UserConsentResponse.builder()
            .id(userConsent.getId())
            .locationAgreed(userConsent.getLocationAgreed())
            .sensitiveInfoAgreed(userConsent.getSensitiveInfoAgreed())
            .sensitiveInfoAgreedAt(userConsent.getSensitiveInfoAgreedAt())
            .updatedAt(userConsent.getUpdatedAt())
            .build();
    }
}
