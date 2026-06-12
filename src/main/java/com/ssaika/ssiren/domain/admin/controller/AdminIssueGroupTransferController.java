package com.ssaika.ssiren.domain.admin.controller;

import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueGroupTransferCreateRequest;
import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueGroupTransferDecisionRequest;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueGroupTransferHistoryListResponse;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueGroupTransferHistoryResponse;
import com.ssaika.ssiren.domain.admin.service.AdminDepartmentTransferService;
import com.ssaika.ssiren.domain.report.entity.IssueGroupTransferHistoryStatus;
import com.ssaika.ssiren.global.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/issues")
public class AdminIssueGroupTransferController {

    private final AdminDepartmentTransferService adminDepartmentTransferService;

    @PostMapping("/{issueGroupId}/transfer-requests")
    public ResponseEntity<BaseResponse<AdminIssueGroupTransferHistoryResponse>> createTransferRequest(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long issueGroupId,
            @Valid @RequestBody AdminIssueGroupTransferCreateRequest request
    ) {
        AdminIssueGroupTransferHistoryResponse response =
                adminDepartmentTransferService.createIssueGroupTransferRequest(userId, issueGroupId, request);

        return ResponseEntity.ok(BaseResponse.success("이슈 그룹 이관 요청 생성 성공", response));
    }

    @GetMapping("/transfer-requests")
    public ResponseEntity<BaseResponse<AdminIssueGroupTransferHistoryListResponse>> getIncomingTransferRequests(
            @AuthenticationPrincipal Long userId
    ) {
        AdminIssueGroupTransferHistoryListResponse response =
                adminDepartmentTransferService.getIncomingIssueGroupTransferRequests(userId);

        return ResponseEntity.ok(BaseResponse.success("이관 요청 목록 조회 성공", response));
    }

    @PatchMapping("/transfer-requests/{transferId}")
    public ResponseEntity<BaseResponse<AdminIssueGroupTransferHistoryResponse>> decideTransferRequest(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long transferId,
            @Valid @RequestBody AdminIssueGroupTransferDecisionRequest request
    ) {
        AdminIssueGroupTransferHistoryResponse response =
                adminDepartmentTransferService.decideIssueGroupTransferRequest(userId, transferId, request);

        return ResponseEntity.ok(BaseResponse.success("이슈 그룹 이관 요청 응답 성공", response));
    }

    @GetMapping("/transfer-requests/sent")
    public ResponseEntity<BaseResponse<AdminIssueGroupTransferHistoryListResponse>> getSentTransferRequests(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) IssueGroupTransferHistoryStatus status
    ) {
        AdminIssueGroupTransferHistoryListResponse response =
                adminDepartmentTransferService.getSentIssueGroupTransferRequests(userId, status);

        return ResponseEntity.ok(BaseResponse.success("내가 신청한 이관 요청 목록 조회 성공", response));
    }
}