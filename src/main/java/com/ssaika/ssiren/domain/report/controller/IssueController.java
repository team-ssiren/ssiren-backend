package com.ssaika.ssiren.domain.report.controller;

import com.ssaika.ssiren.domain.report.dto.response.IssueListResponse;
import com.ssaika.ssiren.domain.report.service.ReportService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import com.ssaika.ssiren.global.enums.IssueGroupStatus;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/issues")
@Validated
public class IssueController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<BaseResponse<IssueListResponse>> getIssues(
        @RequestParam(required = false) BigDecimal latitude,
        @RequestParam(required = false) BigDecimal longitude,
        @RequestParam(required = false) Integer radiusMeters,
        @RequestParam(required = false) BigDecimal swLat,
        @RequestParam(required = false) BigDecimal swLng,
        @RequestParam(required = false) BigDecimal neLat,
        @RequestParam(required = false) BigDecimal neLng,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) Long agencyId,
        @RequestParam(required = false) IssueGroupStatus status,
        @RequestParam(required = false) BigDecimal riskMin,
        @RequestParam(required = false) BigDecimal riskMax,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to) {
        IssueListResponse response = IssueListResponse.from(
            reportService.getIssues(
                latitude,
                longitude,
                radiusMeters,
                swLat,
                swLng,
                neLat,
                neLng,
                categoryId,
                agencyId,
                status,
                riskMin,
                riskMax,
                from,
                to
            )
        );

        return ResponseEntity.ok(BaseResponse.success("이슈 그룹 조회 성공", response));
    }
}
