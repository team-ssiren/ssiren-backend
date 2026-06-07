package com.ssaika.ssiren.domain.user.controller;

import com.ssaika.ssiren.domain.user.dto.request.UserUpdateRequest;
import com.ssaika.ssiren.domain.user.dto.response.UserResponse;
import com.ssaika.ssiren.domain.user.service.UserService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(BaseResponse.success("내 정보 조회 성공", response));
    }

    @PatchMapping("/me")
    public ResponseEntity<BaseResponse<UserResponse>> updateMe(
        @AuthenticationPrincipal Long userId,
        @RequestBody @Valid UserUpdateRequest request) {
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(BaseResponse.success("유저 정보 수정 성공", response));
    }

    @DeleteMapping("/me")
    public ResponseEntity<BaseResponse<UserResponse>> deactivateMe(
        @AuthenticationPrincipal Long userId) {
        UserResponse response = userService.deactivateUser(userId);
        return ResponseEntity.ok(BaseResponse.success("회원 탈퇴 성공", response));
    }
}
