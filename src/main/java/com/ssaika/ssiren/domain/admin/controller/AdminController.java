package com.ssaika.ssiren.domain.admin.controller;

import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueGroupStatusUpdateRequest;
import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueSearchRequest;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueDetailResponse;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueGroupStatusUpdateResponse;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueListResponse;
import com.ssaika.ssiren.domain.admin.service.AdminMapService;
import com.ssaika.ssiren.domain.admin.service.AdminService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/issues")
@Validated
public class AdminController {

    private final AdminMapService adminMapService;
    private final AdminService adminService;

    @GetMapping
    public ResponseEntity<BaseResponse<AdminIssueListResponse>> getAdminIssues(
            @AuthenticationPrincipal Long userId,
            @ModelAttribute AdminIssueSearchRequest request
    ) {
        AdminIssueListResponse response = AdminIssueListResponse.from(
                adminMapService.getAdminIssues(userId, request)
        );

        return ResponseEntity.ok(BaseResponse.success("관리자 지도 이슈 그룹 조회 성공", response));
    }

    @GetMapping("/{issueGroupId}")
    public ResponseEntity<BaseResponse<AdminIssueDetailResponse>> getAdminIssue(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long issueGroupId) {
        AdminIssueDetailResponse response = adminMapService.getAdminIssue(userId, issueGroupId);

        return ResponseEntity.ok(BaseResponse.success("관리자 이슈 그룹 상세 조회 성공", response));
    }

    @PatchMapping("/{issueGroupId}/status")
    public ResponseEntity<BaseResponse<AdminIssueGroupStatusUpdateResponse>> updateAdminIssueGroupStatus(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long issueGroupId,
            @Valid @RequestBody AdminIssueGroupStatusUpdateRequest request
    ) {
        AdminIssueGroupStatusUpdateResponse response =
                adminService.updateAdminIssueGroupStatus(userId, issueGroupId, request);

        return ResponseEntity.ok(BaseResponse.success("이슈 그룹 처리 상태 변경 성공", response));
    }
}