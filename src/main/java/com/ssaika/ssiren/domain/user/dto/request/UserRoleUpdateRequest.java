package com.ssaika.ssiren.domain.user.dto.request;

import com.ssaika.ssiren.global.enums.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserRoleUpdateRequest {

    @NotNull(message = "사용자 권한은 필수입니다.")
    private final UserRole role;

    private final Long departmentId;
}
