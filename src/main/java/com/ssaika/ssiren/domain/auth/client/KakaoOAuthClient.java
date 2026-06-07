package com.ssaika.ssiren.domain.auth.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class KakaoOAuthClient implements OAuthClient {

    private static final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestClient restClient = RestClient.create();

    @Override
    public OAuthUserProfile getUserProfile(String accessToken) {
        try {
            KakaoUserResponse response = restClient.get()
                .uri(USER_INFO_URL)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                    (request, clientResponse) -> {
                        throw new CustomException(ErrorCode.OAUTH_TOKEN_FAILED.getMessage(),
                            ErrorCode.OAUTH_TOKEN_FAILED);
                    })
                .body(KakaoUserResponse.class);

            if (response == null || response.kakaoAccount() == null
                || response.kakaoAccount().email() == null) {
                throw new CustomException(ErrorCode.OAUTH_PROFILE_FAILED.getMessage(),
                    ErrorCode.OAUTH_PROFILE_FAILED);
            }

            return new OAuthUserProfile(
                response.kakaoAccount().email(),
                resolveNickname(response)
            );
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.OAUTH_TOKEN_FAILED.getMessage(),
                ErrorCode.OAUTH_TOKEN_FAILED);
        }
    }

    @Override
    public String getProviderName() {
        return "kakao";
    }

    private record KakaoUserResponse(
        KakaoProperties properties,
        @JsonProperty("kakao_account") KakaoAccount kakaoAccount
    ) {

        private record KakaoProperties(String nickname) {
        }

        private record KakaoAccount(String email, KakaoProfile profile) {
        }

        private record KakaoProfile(String nickname) {
        }
    }

    private String resolveNickname(KakaoUserResponse response) {
        if (response.properties() != null && response.properties().nickname() != null) {
            return response.properties().nickname();
        }
        if (response.kakaoAccount().profile() != null
            && response.kakaoAccount().profile().nickname() != null) {
            return response.kakaoAccount().profile().nickname();
        }
        return response.kakaoAccount().email();
    }
}
