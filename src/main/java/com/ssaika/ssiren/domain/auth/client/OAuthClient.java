package com.ssaika.ssiren.domain.auth.client;

public interface OAuthClient {

    OAuthUserProfile getUserProfile(String token);

    String getProviderName();

    record OAuthUserProfile(String email, String nickname) {
    }
}
