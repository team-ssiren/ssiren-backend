package com.ssaika.ssiren.domain.admin.controller;

import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueDetailResponse;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueListResponse;
import com.ssaika.ssiren.domain.admin.service.AdminMapService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import com.ssaika.ssiren.global.enums.IssueGroupStatus;
import com.ssaika.ssiren.global.enums.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/issues")
@Validated
public class AdminController {

    private final AdminMapService adminMapService;

    @GetMapping
    public ResponseEntity<BaseResponse<AdminIssueListResponse>> getAdminIssues(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(required = false) Integer radiusMeters,
            @RequestParam(required = false) BigDecimal swLat,
            @RequestParam(required = false) BigDecimal swLng,
            @RequestParam(required = false) BigDecimal neLat,
            @RequestParam(required = false) BigDecimal neLng,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long agencyTypeId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean myDepartmentOnly,
            @RequestParam(required = false) Boolean deletedOnly,
            @RequestParam(required = false) IssueGroupStatus status,
            @RequestParam(required = false) ReportStatus reportStatus,
            @RequestParam(required = false) BigDecimal riskMin,
            @RequestParam(required = false) BigDecimal riskMax,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        AdminIssueListResponse response = AdminIssueListResponse.from(
                adminMapService.getAdminIssues(
                        userId,
                        latitude,
                        longitude,
                        radiusMeters,
                        swLat,
                        swLng,
                        neLat,
                        neLng,
                        categoryId,
                        agencyTypeId,
                        departmentId,
                        myDepartmentOnly,
                        deletedOnly,
                        status,
                        reportStatus,
                        riskMin,
                        riskMax,
                        from,
                        to
                )
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
}