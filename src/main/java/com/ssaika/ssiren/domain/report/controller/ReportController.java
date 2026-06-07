package com.ssaika.ssiren.domain.report.controller;

import com.ssaika.ssiren.domain.report.dto.response.MyReportResponse;
import com.ssaika.ssiren.domain.report.service.ReportService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import com.ssaika.ssiren.global.dto.PageResponseDto;
import com.ssaika.ssiren.global.enums.ReportStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
}
