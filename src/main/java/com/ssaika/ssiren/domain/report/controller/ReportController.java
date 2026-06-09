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

    private static final Long TEST_USER_ID = 1L;

    private final ReportService reportService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ReportCreateResponse>> createReport(
        @AuthenticationPrincipal Long userId,
        @RequestParam("request") String requestJson,
        @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        // TODO: 로그인 완성 후 Spring Security 기반 인증 사용자 ID를 사용하도록 되돌린다.
        ReportCreateRequest request = parseReportCreateRequest(requestJson);
        ReportCreateResponse response = reportService.createReport(TEST_USER_ID, request, images);

        return ResponseEntity.ok(BaseResponse.success("제보 등록 성공", response));
    }

    @PostMapping(value = "/drafts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse<ReportDraftCreateResponse>> createReportDraft(
        @AuthenticationPrincipal Long userId,
        @ModelAttribute @Valid ReportDraftRequest request) {
        ReportDraftCreateResponse response = reportService.createReportDraft(userId, request);

        return ResponseEntity.ok(BaseResponse.success("제보 초안 생성 성공", response));
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

    private ReportCreateRequest parseReportCreateRequest(String requestJson) {
        try {
            return objectMapper.readValue(requestJson, ReportCreateRequest.class);
        } catch (JsonProcessingException e) {
            throw new CustomException("제보 등록 요청 JSON 형식이 올바르지 않습니다.", ErrorCode.INVALID_FORMAT);
        }
    }
}
