package com.ssaika.ssiren.domain.user.controller;

import com.ssaika.ssiren.domain.user.dto.response.UserResponse;
import com.ssaika.ssiren.domain.user.service.UserService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<UserResponse>> getMe(
        @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED.getMessage(), ErrorCode.UNAUTHORIZED);
        }

        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(BaseResponse.success("내 정보 조회 성공", response));
    }
}
