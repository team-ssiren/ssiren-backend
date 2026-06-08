package com.ssaika.ssiren.domain.report.controller;

import com.ssaika.ssiren.domain.report.dto.request.MyReportUpdateRequest;
import com.ssaika.ssiren.domain.report.dto.request.ReportReactionRequest;
import com.ssaika.ssiren.domain.report.dto.response.*;
import com.ssaika.ssiren.domain.report.service.ReportService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import com.ssaika.ssiren.global.dto.ListResponseDto;
import com.ssaika.ssiren.global.dto.PageResponseDto;
import com.ssaika.ssiren.global.enums.ReportStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@Validated
public class ReportController {

    private static final Long TEST_USER_ID = 1L;

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<BaseResponse<PageResponseDto<ReportListResponse>>> getReports(
        @RequestParam(required = false) ReportStatus status,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String sido,
        @RequestParam(required = false) String sigungu,
        @RequestParam(required = false) String eupmyeondong,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable) {
        PageResponseDto<ReportListResponse> response = PageResponseDto.from(
            reportService.getReports(status, categoryId, sido, sigungu, eupmyeondong, from, to, pageable)
        );

        return ResponseEntity.ok(BaseResponse.success("전체 제보 목록 조회 성공", response));
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<PageResponseDto<MyReportResponse>>> getMyReports(
        @RequestParam(required = false) ReportStatus status,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable) {
        PageResponseDto<MyReportResponse> response = PageResponseDto.from(
            reportService.getMyReports(TEST_USER_ID, status, categoryId, from, to, pageable)
        );

        return ResponseEntity.ok(BaseResponse.success("내 제보 목록 조회 성공", response));
    }
    @GetMapping("/me/{reportId}")
    public ResponseEntity<BaseResponse<MyReportDetailResponse>> getMyReport(
        @PathVariable Long reportId) {
        MyReportDetailResponse response = reportService.getMyReport(TEST_USER_ID, reportId);

        return ResponseEntity.ok(BaseResponse.success("내 제보 상세 조회 성공", response));
    }
    @PatchMapping("/me/{reportId}")
    public ResponseEntity<BaseResponse<MyReportUpdateResponse>> updateMyReport(
        @PathVariable Long reportId,
        @RequestBody @Valid MyReportUpdateRequest request) {
        MyReportUpdateResponse response = reportService.updateMyReport(TEST_USER_ID, reportId, request);

        return ResponseEntity.ok(BaseResponse.success("내 제보 수정 성공", response));
    }
    @DeleteMapping("/me/{reportId}")
    public ResponseEntity<BaseResponse<MyReportDeleteResponse>> deleteMyReport(
        @PathVariable Long reportId) {
        MyReportDeleteResponse response = reportService.deleteMyReport(TEST_USER_ID, reportId);

        return ResponseEntity.ok(BaseResponse.success("내 제보 삭제 성공", response));
    }
    @PostMapping("/{reportId}/reactions")
    public ResponseEntity<BaseResponse<ReportReactionResponse>> saveReportReaction(
        @PathVariable Long reportId,
        @RequestBody @Valid ReportReactionRequest request) {
        ReportReactionResponse response = reportService.saveReportReaction(
            TEST_USER_ID,
            reportId,
            request
        );

        return ResponseEntity.ok(BaseResponse.success("제보 반응 반영 성공", response));
    }

    @GetMapping("/categories")
    public ResponseEntity<BaseResponse<ListResponseDto<ReportCategoryResponse>>> getReportCategories() {
        ListResponseDto<ReportCategoryResponse> response = ListResponseDto.from(
                reportService.getReportCategories()
        );

        return ResponseEntity.ok(BaseResponse.success("제보 카테고리 목록 조회 성공", response));
    }
}
