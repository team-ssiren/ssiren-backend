package com.ssaika.ssiren.domain.user.service;

import com.ssaika.ssiren.domain.user.dto.request.UserConsentUpdateRequest;
import com.ssaika.ssiren.domain.user.dto.request.UserUpdateRequest;
import com.ssaika.ssiren.domain.user.dto.response.UserConsentResponse;
import com.ssaika.ssiren.domain.user.dto.response.UserResponse;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.entity.UserConsent;
import com.ssaika.ssiren.domain.user.repository.UserConsentRepository;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserConsentRepository userConsentRepository;

    public UserResponse getUserById(Long userId) {
        User user = findUserById(userId);

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = findUserById(userId);

        user.updateProfile(request.getNickname(), request.getIsAlarmEnabled());
        userRepository.flush();

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse deactivateUser(Long userId) {
        User user = findUserById(userId);

        user.deactivate();
        userRepository.flush();

        return UserResponse.from(user);
    }

    @Transactional
    public UserConsentResponse updateUserConsent(
        Long userId,
        UserConsentUpdateRequest request) {
        User user = findUserById(userId);

        UserConsent userConsent = userConsentRepository.findByUserId(userId)
            .map(consent -> {
                consent.update(request.getLocationAgreed(), request.getSensitiveInfoAgreed());
                return consent;
            })
            .orElseGet(() -> userConsentRepository.save(UserConsent.create(
                user,
                request.getLocationAgreed(),
                request.getSensitiveInfoAgreed()
            )));

        userConsentRepository.flush();

        return UserConsentResponse.from(userConsent);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                ErrorCode.USER_NOT_FOUND));
    }
}
