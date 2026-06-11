package com.ssaika.ssiren.domain.auth.service;

import com.ssaika.ssiren.domain.auth.client.OAuthClient;
import com.ssaika.ssiren.domain.auth.client.OAuthClient.OAuthUserProfile;
import com.ssaika.ssiren.domain.auth.dto.response.TokenResponse;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.entity.UserConsent;
import com.ssaika.ssiren.domain.user.repository.UserConsentRepository;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import com.ssaika.ssiren.global.security.JwtProvider;
import com.ssaika.ssiren.global.security.RefreshTokenRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final List<OAuthClient> oauthClients;
    private final UserRepository userRepository;
    private final UserConsentRepository userConsentRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse login(String provider, String providerToken) {
        OAuthClient client = findClient(provider);
        OAuthUserProfile userProfile = client.getUserProfile(providerToken);

        Optional<User> existingUser = userRepository.findByEmail(userProfile.email());
        boolean isNewUser = existingUser.isEmpty();
        User user = existingUser.orElseGet(() -> createUserWithDefaultConsent(userProfile));
        ensureDefaultConsent(user);

        return issueTokens(user.getId(), isNewUser);
    }

    @Transactional(readOnly = true)
    public TokenResponse refreshToken(String refreshToken) {
        Long userId = jwtProvider.extractUserId(refreshToken);

        String storedRefreshToken = refreshTokenRepository.find(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND.getMessage(),
                ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!refreshToken.equals(storedRefreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN.getMessage(),
                ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String newAccessToken = jwtProvider.generateAccessToken(userId);

        return TokenResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .isNewUser(false)
            .build();
    }

    public void logout(Long userId) {
        refreshTokenRepository.delete(userId);
    }

    private OAuthClient findClient(String provider) {
        return oauthClients.stream()
            .filter(client -> client.getProviderName().equalsIgnoreCase(provider))
            .findFirst()
            .orElseThrow(() -> new CustomException(ErrorCode.UNSUPPORTED_PROVIDER.getMessage(),
                ErrorCode.UNSUPPORTED_PROVIDER));
    }

    private User createUserWithDefaultConsent(OAuthUserProfile userProfile) {
        User user = userRepository.save(
            User.createKakaoUser(userProfile.email(), userProfile.nickname()));
        userConsentRepository.save(UserConsent.create(user, false, false));

        return user;
    }

    private void ensureDefaultConsent(User user) {
        if (userConsentRepository.findFirstByUserIdOrderByUpdatedAtDesc(user.getId()).isEmpty()) {
            userConsentRepository.save(UserConsent.create(user, false, false));
        }
    }

    private TokenResponse issueTokens(Long userId, boolean isNewUser) {
        String accessToken = jwtProvider.generateAccessToken(userId);
        String refreshToken = jwtProvider.generateRefreshToken(userId);

        refreshTokenRepository.save(userId, refreshToken, jwtProvider.getRefreshExpiry() / 1000);

        return TokenResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .isNewUser(isNewUser)
            .build();
    }
}
