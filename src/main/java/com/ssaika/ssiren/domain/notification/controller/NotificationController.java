package com.ssaika.ssiren.domain.notification.controller;

import com.ssaika.ssiren.domain.notification.dto.request.FcmTokenRequest;
import com.ssaika.ssiren.domain.notification.dto.response.FcmTokenResponse;
import com.ssaika.ssiren.domain.notification.service.NotificationService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
@Validated
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/tokens")
    public ResponseEntity<BaseResponse<FcmTokenResponse>> registerFcmToken(
        @AuthenticationPrincipal Long userId,
        @RequestBody @Valid FcmTokenRequest request) {
        FcmTokenResponse response = notificationService.registerFcmToken(userId, request);
        return ResponseEntity.ok(BaseResponse.success("FCM 토큰 등록 성공", response));
    }

    @DeleteMapping("/tokens")
    public ResponseEntity<BaseResponse<Void>> deactivateFcmToken(
        @AuthenticationPrincipal Long userId,
        @RequestBody @Valid FcmTokenRequest request) {
        notificationService.deactivateFcmToken(userId, request);
        return ResponseEntity.ok(
            BaseResponse.success(HttpStatus.OK, "FCM 토큰 비활성화 성공", null));
    }
}
