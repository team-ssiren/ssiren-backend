package com.ssaika.ssiren.domain.admin.controller;

import com.ssaika.ssiren.domain.admin.dto.request.AdminDashboardDenseAreaRequest;
import com.ssaika.ssiren.domain.admin.dto.response.AdminDashboardCategoryStatisticsResponse;
import com.ssaika.ssiren.domain.admin.dto.response.AdminDashboardDenseAreaResponse;
import com.ssaika.ssiren.domain.admin.dto.response.AdminDashboardStatisticsResponse;
import com.ssaika.ssiren.domain.admin.service.AdminDashBoardService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/dashboard")
@Validated
public class AdminDashboardController {

    private final AdminDashBoardService adminDashBoardService;

    @GetMapping("/statistics")
    public ResponseEntity<BaseResponse<AdminDashboardStatisticsResponse>> getStatistics(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Boolean myDepartmentOnly
    ) {
        AdminDashboardStatisticsResponse response = adminDashBoardService.getStatistics(userId, myDepartmentOnly);

        return ResponseEntity.ok(BaseResponse.success("대시보드 메인 통계 요약 조회 성공", response));
    }

    @GetMapping("/categories")
    public ResponseEntity<BaseResponse<AdminDashboardCategoryStatisticsResponse>> getCategoryStatistics(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) Boolean myDepartmentOnly
    ) {
        AdminDashboardCategoryStatisticsResponse response =
                adminDashBoardService.getCategoryStatistics(userId, myDepartmentOnly);

        return ResponseEntity.ok(BaseResponse.success("유형별 제보 통계 조회 성공", response));
    }

    @GetMapping("/dense-area")
    public ResponseEntity<BaseResponse<AdminDashboardDenseAreaResponse>> getDenseAreas(
            @AuthenticationPrincipal Long userId,
            @ModelAttribute @Valid AdminDashboardDenseAreaRequest request
    ) {
        AdminDashboardDenseAreaResponse response =
                adminDashBoardService.getDenseAreas(userId, request);

        return ResponseEntity.ok(BaseResponse.success("밀집 구역 조회 성공", response));
    }
}