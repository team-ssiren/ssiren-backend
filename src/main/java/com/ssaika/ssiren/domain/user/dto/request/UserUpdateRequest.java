package com.ssaika.ssiren.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
public class UserUpdateRequest {

    @Pattern(regexp = ".*\\S.*", message = "닉네임은 공백일 수 없습니다.")
    @Size(max = 50, message = "닉네임은 50자 이하여야 합니다.")
    private final String nickname;

    private final Boolean isAlarmEnabled;
}
