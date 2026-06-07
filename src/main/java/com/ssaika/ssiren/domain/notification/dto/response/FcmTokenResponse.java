package com.ssaika.ssiren.domain.notification.dto.response;

import com.ssaika.ssiren.domain.user.entity.UserFcmToken;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmTokenResponse {

    private final Long id;
    private final String fcmToken;
    private final Boolean isActive;
    private final LocalDateTime createdAt;

    public static FcmTokenResponse from(UserFcmToken userFcmToken) {
        return FcmTokenResponse.builder()
            .id(userFcmToken.getId())
            .fcmToken(userFcmToken.getFcmToken())
            .isActive(userFcmToken.getIsActive())
            .createdAt(userFcmToken.getCreatedAt())
            .build();
    }
}
