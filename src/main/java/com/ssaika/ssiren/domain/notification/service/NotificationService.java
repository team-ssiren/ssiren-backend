package com.ssaika.ssiren.domain.notification.service;

import com.ssaika.ssiren.domain.notification.dto.request.FcmTokenRequest;
import com.ssaika.ssiren.domain.notification.dto.response.FcmTokenResponse;
import com.ssaika.ssiren.domain.notification.service.FcmPushService.FcmSendResult;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.entity.UserFcmToken;
import com.ssaika.ssiren.domain.user.repository.UserFcmTokenRepository;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final UserRepository userRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final FcmPushService fcmPushService;

    @Transactional
    public FcmTokenResponse registerFcmToken(Long userId, FcmTokenRequest request) {
        User user = findUserById(userId);

        UserFcmToken userFcmToken = userFcmTokenRepository.findByFcmToken(request.getFcmToken())
            .map(existingToken -> {
                existingToken.activate(user);
                return existingToken;
            })
            .orElseGet(() -> userFcmTokenRepository.save(
                UserFcmToken.create(user, request.getFcmToken())));

        userFcmTokenRepository.flush();

        return FcmTokenResponse.from(userFcmToken);
    }

    @Transactional
    public void deactivateFcmToken(Long userId, FcmTokenRequest request) {
        UserFcmToken userFcmToken = userFcmTokenRepository
            .findByFcmTokenAndUserId(request.getFcmToken(), userId)
            .orElseThrow(() -> new CustomException("토큰을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

        userFcmToken.deactivate();
    }

    @Transactional
    public List<FcmSendResult> sendPushToUser(
        Long userId,
        String title,
        String body,
        Map<String, String> data) {
        User user = findUserById(userId);

        if (!Boolean.TRUE.equals(user.getIsAlarmEnabled())) {
            return List.of();
        }

        return userFcmTokenRepository.findAllByUserIdAndIsActiveTrue(userId).stream()
            .map(token -> sendPushToToken(token, title, body, data))
            .toList();
    }

    private FcmSendResult sendPushToToken(
        UserFcmToken userFcmToken,
        String title,
        String body,
        Map<String, String> data) {
        FcmSendResult result = fcmPushService.sendToToken(
            userFcmToken.getFcmToken(),
            title,
            body,
            data
        );

        if (result.invalidToken()) {
            userFcmToken.deactivate();
        }

        return result;
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                ErrorCode.USER_NOT_FOUND));
    }
}
