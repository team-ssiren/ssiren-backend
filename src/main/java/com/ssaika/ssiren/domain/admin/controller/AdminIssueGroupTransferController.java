package com.ssaika.ssiren.domain.admin.controller;

import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueGroupTransferCreateRequest;
import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueGroupTransferResponseRequest;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueGroupTransferHistoryListResponse;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueGroupTransferHistoryResponse;
import com.ssaika.ssiren.domain.admin.service.AdminDepartmentTransferService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
public class AdminIssueGroupTransferController {

    private final AdminDepartmentTransferService adminDepartmentTransferService;

    @PostMapping("/issues/{issueGroupId}/department")
    public ResponseEntity<BaseResponse<AdminIssueGroupTransferHistoryResponse>> createTransferHistory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long issueGroupId,
            @Valid @RequestBody AdminIssueGroupTransferCreateRequest request
    ) {
        AdminIssueGroupTransferHistoryResponse response =
                adminDepartmentTransferService.createIssueGroupTransferHistory(userId, issueGroupId, request);

        return ResponseEntity.ok(BaseResponse.success("이슈 그룹 이관 요청 생성 성공", response));
    }

    @GetMapping("/transfer-histories")
    public ResponseEntity<BaseResponse<AdminIssueGroupTransferHistoryListResponse>> getIncomingTransferHistories(
            @AuthenticationPrincipal Long userId
    ) {
        AdminIssueGroupTransferHistoryListResponse response =
                adminDepartmentTransferService.getIncomingIssueGroupTransferHistories(userId);

        return ResponseEntity.ok(BaseResponse.success("이관 요청 목록 조회 성공", response));
    }

    @PatchMapping("/transfer-histories/{transferHistoryId}/accept")
    public ResponseEntity<BaseResponse<AdminIssueGroupTransferHistoryResponse>> acceptTransferHistory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long transferHistoryId,
            @Valid @RequestBody AdminIssueGroupTransferResponseRequest request
    ) {
        AdminIssueGroupTransferHistoryResponse response =
                adminDepartmentTransferService.acceptIssueGroupTransferHistory(userId, transferHistoryId, request);

        return ResponseEntity.ok(BaseResponse.success("이슈 그룹 이관 요청 수락 성공", response));
    }

    @PatchMapping("/transfer-histories/{transferHistoryId}/reject")
    public ResponseEntity<BaseResponse<AdminIssueGroupTransferHistoryResponse>> rejectTransferHistory(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long transferHistoryId,
            @Valid @RequestBody AdminIssueGroupTransferResponseRequest request
    ) {
        AdminIssueGroupTransferHistoryResponse response =
                adminDepartmentTransferService.rejectIssueGroupTransferHistory(userId, transferHistoryId, request);

        return ResponseEntity.ok(BaseResponse.success("이슈 그룹 이관 요청 반려 성공", response));
    }
}