package com.ssaika.ssiren.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {

    private final String accessToken;
    private final String refreshToken;
    private final String tokenType;
    private final boolean isNewUser;

    @JsonProperty("isNewUser")
    public boolean isNewUser() {
        return isNewUser;
    }
}
