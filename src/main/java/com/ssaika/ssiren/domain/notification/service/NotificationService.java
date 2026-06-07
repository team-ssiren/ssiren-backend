package com.ssaika.ssiren.domain.notification.service;

import com.ssaika.ssiren.domain.notification.dto.request.FcmTokenRequest;
import com.ssaika.ssiren.domain.notification.dto.response.FcmTokenResponse;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.entity.UserFcmToken;
import com.ssaika.ssiren.domain.user.repository.UserFcmTokenRepository;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final UserRepository userRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;

    @Transactional
    public FcmTokenResponse registerFcmToken(Long userId, FcmTokenRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                ErrorCode.USER_NOT_FOUND));

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
}
