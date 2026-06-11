package com.ssaika.ssiren.domain.user.dto.response;

import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.global.enums.UserRole;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {

    private final Long id;
    private final String email;
    private final String nickname;
    private final UserRole role;
    private final Boolean roleSelected;
    private final Boolean isActive;
    private final Boolean isAlarmEnabled;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .nickname(user.getNickname())
            .role(user.getRole())
            .roleSelected(user.getRoleSelected())
            .isActive(user.getIsActive())
            .isAlarmEnabled(user.getIsAlarmEnabled())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
