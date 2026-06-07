package com.ssaika.ssiren.domain.auth.controller;

import com.ssaika.ssiren.domain.auth.dto.request.OAuthLoginRequest;
import com.ssaika.ssiren.domain.auth.dto.request.TokenRefreshRequest;
import com.ssaika.ssiren.domain.auth.dto.response.TokenResponse;
import com.ssaika.ssiren.domain.auth.service.AuthService;
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
@RequestMapping("/api/v1/auth")
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<BaseResponse<TokenResponse>> login(
        @RequestBody @Valid OAuthLoginRequest request) {
        TokenResponse tokenResponse = authService.login(request.getProvider(),
            request.getProviderToken());

        if (tokenResponse.isNewUser()) {
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(HttpStatus.CREATED, "회원가입에 성공했습니다.", tokenResponse));
        }

        return ResponseEntity.ok(BaseResponse.success("로그인에 성공했습니다.", tokenResponse));
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<BaseResponse<TokenResponse>> refresh(
        @RequestBody @Valid TokenRefreshRequest request) {
        TokenResponse tokenResponse = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(BaseResponse.success("토큰 재발급에 성공했습니다.", tokenResponse));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<BaseResponse<Void>> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ResponseEntity.ok(BaseResponse.success("로그아웃에 성공했습니다."));
    }
}
