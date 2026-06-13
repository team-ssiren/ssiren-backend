package com.ssaika.ssiren.domain.report.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.dto.request.MyReportUpdateRequest;
import com.ssaika.ssiren.domain.report.dto.request.ReportCreateRequest;
import com.ssaika.ssiren.domain.report.dto.request.ReportDraftRequest;
import com.ssaika.ssiren.domain.report.dto.request.ReportReactionRequest;
import com.ssaika.ssiren.domain.report.dto.response.MyReportDeleteResponse;
import com.ssaika.ssiren.domain.report.dto.response.MyReportDetailResponse;
import com.ssaika.ssiren.domain.report.dto.response.MyReportResponse;
import com.ssaika.ssiren.domain.report.dto.response.MyReportUpdateResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportCategoryResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportCreateResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportDraftCreateResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportListResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportReactionResponse;
import com.ssaika.ssiren.domain.report.service.ReportService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import com.ssaika.ssiren.global.dto.ListResponseDto;
import com.ssaika.ssiren.global.dto.PageResponseDto;
import com.ssaika.ssiren.global.enums.ReportStatus;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@Validated
public class ReportController {

    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ReportCreateResponse>> createReport(
        @AuthenticationPrincipal Long userId,
        @RequestParam("request") String requestJson,
        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ReportCreateRequest request = parseReportCreateRequest(requestJson);
        ReportCreateResponse response = reportService.createReport(userId, request, images);

        return ResponseEntity.ok(BaseResponse.success("?쒕낫 ?깅줉 ?깃났", response));
    }

    @PostMapping(value = "/drafts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ReportDraftCreateResponse>> createReportDraft(
        @AuthenticationPrincipal Long userId,
        @ModelAttribute @Valid ReportDraftRequest request) {
        ReportDraftCreateResponse response = reportService.createReportDraft(userId, request);

        return ResponseEntity.ok(BaseResponse.success("?쒕낫 珥덉븞 ?앹꽦 ?깃났", response));
    }

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

        return ResponseEntity.ok(BaseResponse.success("?꾩껜 ?쒕낫 紐⑸줉 議고쉶 ?깃났", response));
    }

    @GetMapping("/me")
    public ResponseEntity<BaseResponse<PageResponseDto<MyReportResponse>>> getMyReports(
        @AuthenticationPrincipal Long userId,
        @RequestParam(required = false) ReportStatus status,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false) String from,
        @RequestParam(required = false) String to,
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable) {
        PageResponseDto<MyReportResponse> response = PageResponseDto.from(
            reportService.getMyReports(userId, status, categoryId, from, to, pageable)
        );

        return ResponseEntity.ok(BaseResponse.success("???쒕낫 紐⑸줉 議고쉶 ?깃났", response));
    }
    @GetMapping("/me/{reportId}")
    public ResponseEntity<BaseResponse<MyReportDetailResponse>> getMyReport(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long reportId) {
        MyReportDetailResponse response = reportService.getMyReport(userId, reportId);

        return ResponseEntity.ok(BaseResponse.success("???쒕낫 ?곸꽭 議고쉶 ?깃났", response));
    }
    @PatchMapping("/me/{reportId}")
    public ResponseEntity<BaseResponse<MyReportUpdateResponse>> updateMyReport(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long reportId,
        @RequestBody @Valid MyReportUpdateRequest request) {
        MyReportUpdateResponse response = reportService.updateMyReport(userId, reportId, request);

        return ResponseEntity.ok(BaseResponse.success("???쒕낫 ?섏젙 ?깃났", response));
    }
    @DeleteMapping("/me/{reportId}")
    public ResponseEntity<BaseResponse<MyReportDeleteResponse>> deleteMyReport(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long reportId) {
        MyReportDeleteResponse response = reportService.deleteMyReport(userId, reportId);

        return ResponseEntity.ok(BaseResponse.success("???쒕낫 ??젣 ?깃났", response));
    }
    @PostMapping("/{reportId}/reactions")
    public ResponseEntity<BaseResponse<ReportReactionResponse>> saveReportReaction(
        @AuthenticationPrincipal Long userId,
        @PathVariable Long reportId,
        @RequestBody @Valid ReportReactionRequest request) {
        ReportReactionResponse response = reportService.saveReportReaction(
            userId,
            reportId,
            request
        );

        return ResponseEntity.ok(BaseResponse.success("공감이 반영됐어요", response));
    }

    @GetMapping("/categories")
    public ResponseEntity<BaseResponse<ListResponseDto<ReportCategoryResponse>>> getReportCategories() {
        ListResponseDto<ReportCategoryResponse> response = ListResponseDto.from(
                reportService.getReportCategories()
        );

        return ResponseEntity.ok(BaseResponse.success("?쒕낫 移댄뀒怨좊━ 紐⑸줉 議고쉶 ?깃났", response));
    }

    private ReportCreateRequest parseReportCreateRequest(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, ReportCreateRequest.class);
        } catch (JsonProcessingException e) {
            throw new CustomException("?쒕낫 ?깅줉 ?붿껌 JSON ?뺤떇???щ컮瑜댁? ?딆뒿?덈떎.", ErrorCode.INVALID_FORMAT);
        }
    }
}
