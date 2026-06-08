package com.ssaika.ssiren.domain.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.ssaika.ssiren.domain.report.client.ReportAiClient;
import com.ssaika.ssiren.domain.report.client.dto.request.ReportAiAnalyzeRequest;
import com.ssaika.ssiren.domain.report.client.dto.response.ReportAiAnalyzeResponse;
import com.ssaika.ssiren.domain.report.dto.request.ReportDraftRequest;
import com.ssaika.ssiren.domain.report.dto.response.MyReportDetailResponse;
import com.ssaika.ssiren.domain.report.dto.response.MyReportResponse;
import com.ssaika.ssiren.domain.report.dto.request.MyReportUpdateRequest;
import com.ssaika.ssiren.domain.report.dto.request.ReportReactionRequest;
import com.ssaika.ssiren.domain.report.dto.response.MyReportDeleteResponse;
import com.ssaika.ssiren.domain.report.dto.response.MyReportUpdateResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportAgencyTypeResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportAiAnalysisResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportCategoryResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportDepartmentResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportDraftCreateResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportDraftResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportReactionResponse;
import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportCategory;
import com.ssaika.ssiren.domain.report.entity.ReportImage;
import com.ssaika.ssiren.domain.report.entity.ReportReactionLog;
import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import com.ssaika.ssiren.domain.report.repository.ReportCategoryRepository;
import com.ssaika.ssiren.domain.report.repository.IssueGroupRepository;
import com.ssaika.ssiren.domain.report.repository.ReportImageRepository;
import com.ssaika.ssiren.domain.report.repository.ReportReactionLogRepository;
import com.ssaika.ssiren.domain.report.repository.ReportRepository;
import com.ssaika.ssiren.domain.report.repository.ReportSpecification;
import com.ssaika.ssiren.domain.report.repository.ReportStatusHistoryRepository;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.enums.ReportStatus;
import com.ssaika.ssiren.global.enums.ReportVisibility;
import com.ssaika.ssiren.global.enums.ReportReactionType;
import com.ssaika.ssiren.domain.report.dto.response.ReportListResponse;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final int MAX_REPORT_DRAFT_IMAGE_COUNT = 5;
    private static final long MAX_REPORT_DRAFT_IMAGE_SIZE = 50 * 1024 * 1024;
    private static final String ADDRESS_NOT_RESOLVED = "주소 확인 필요";

    private final ReportRepository reportRepository;
    private final IssueGroupRepository issueGroupRepository;
    private final ReportCategoryRepository reportCategoryRepository;
    private final ReportImageRepository reportImageRepository;
    private final ReportStatusHistoryRepository reportStatusHistoryRepository;
    private final ReportReactionLogRepository reportReactionLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final ReportAiClient reportAiClient;

    public ReportDraftCreateResponse createReportDraft(Long userId, ReportDraftRequest request) {
        validateAuthenticatedUser(userId);
        validateReportDraftRequest(request);
        log.info(
            "Create report draft. userId={}, latitude={}, longitude={}, imageCount={}",
            userId,
            request.latitude(),
            request.longitude(),
            request.images() == null ? 0 : request.images().size()
        );

        userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(), ErrorCode.USER_NOT_FOUND));

        LocalDateTime occurredAt = request.occurredAt() == null ? LocalDateTime.now() : request.occurredAt();
        AddressSnapshot address = resolveAddressSnapshot(request.latitude(), request.longitude());
        ReportAiAnalyzeResponse aiResponse = reportAiClient.analyzeReport(new ReportAiAnalyzeRequest(
            request.content(),
            request.latitude(),
            request.longitude(),
            occurredAt,
            null,
            null,
            null,
            null,
            request.images()
        ));

        ReportCategory category = resolveReportCategory(aiResponse);

        return new ReportDraftCreateResponse(
            createReportDraftResponse(
                userId,
                request,
                occurredAt,
                address,
                aiResponse,
                category
            ),
            ReportCategoryResponse.from(category),
            category.getParentCategory() == null ? null : ReportCategoryResponse.from(category.getParentCategory()),
            ReportDepartmentResponse.from(category.getDepartment()),
            ReportAgencyTypeResponse.from(category.getDepartment().getAgencyType()),
            ReportAiAnalysisResponse.from(aiResponse.analysis())
        );
    }

    public Page<ReportListResponse> getReports(
        ReportStatus status,
        Long categoryId,
        String sido,
        String sigungu,
        String eupmyeondong,
        String from,
        String to,
        Pageable pageable) {
        LocalDateTime fromDateTime = parseFromDateTime(from);
        LocalDateTime toDateTime = parseToDateTime(to);
        validateDateRange(fromDateTime, toDateTime);
        log.info(
            "Get public reports. status={}, categoryId={}, sido={}, sigungu={}, eupmyeondong={}, "
                + "from={}, to={}, page={}, size={}",
            status,
            categoryId,
            sido,
            sigungu,
            eupmyeondong,
            fromDateTime,
            toDateTime,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );

        Specification<Report> specification = ReportSpecification.hasVisibility(ReportVisibility.PUBLIC)
            .and(ReportSpecification.isNotDeleted())
            .and(ReportSpecification.hasStatus(status))
            .and(ReportSpecification.hasCategory(categoryId))
            .and(ReportSpecification.hasSido(sido))
            .and(ReportSpecification.hasSigungu(sigungu))
            .and(ReportSpecification.hasEupmyeondong(eupmyeondong))
            .and(ReportSpecification.createdAtFrom(fromDateTime))
            .and(ReportSpecification.createdAtTo(toDateTime));

        Page<Report> reports = reportRepository.findAll(specification, pageable);
        Map<Long, List<ReportImage>> reportImages = getReportImages(reports.getContent());

        return reports.map(report -> ReportListResponse.from(
            report,
            reportImages.getOrDefault(report.getId(), List.of()),
            objectMapper
        ));
    }

    public Page<MyReportResponse> getMyReports(
        Long userId,
        ReportStatus status,
        Long categoryId,
        String from,
        String to,
        Pageable pageable) {
        LocalDateTime fromDateTime = parseFromDateTime(from);
        LocalDateTime toDateTime = parseToDateTime(to);
        validateDateRange(fromDateTime, toDateTime);
        log.info(
            "Get my reports. userId={}, status={}, categoryId={}, from={}, to={}, page={}, size={}",
            userId,
            status,
            categoryId,
            fromDateTime,
            toDateTime,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );

        Specification<Report> specification = ReportSpecification.belongsToUser(userId)
            .and(ReportSpecification.hasStatus(status))
            .and(ReportSpecification.hasCategory(categoryId))
            .and(ReportSpecification.createdAtFrom(fromDateTime))
            .and(ReportSpecification.createdAtTo(toDateTime));

        Page<Report> reports = reportRepository.findAll(specification, pageable);
        Map<Long, List<ReportImage>> reportImages = getReportImages(reports.getContent());

        return reports.map(report -> MyReportResponse.from(
            report,
            reportImages.getOrDefault(report.getId(), List.of()),
            objectMapper
        ));
    }

    public MyReportDetailResponse getMyReport(Long userId, Long reportId) {
        log.info("Get my report detail. userId={}, reportId={}", userId, reportId);

        Report report = reportRepository.findByIdAndUser_Id(reportId, userId)
            .orElseThrow(() -> new CustomException("제보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
        List<ReportImage> reportImages = reportImageRepository.findByReport_IdOrderBySortOrderAsc(reportId);
        List<ReportStatusHistory> statusHistories =
            reportStatusHistoryRepository.findByReport_IdOrderByCreatedAtAsc(reportId);
        List<ReportReactionLog> reactionLogs =
            reportReactionLogRepository.findByReport_IdOrderByCreatedAtAsc(reportId);

        return MyReportDetailResponse.from(
            report,
            reportImages,
            statusHistories,
            reactionLogs,
            objectMapper
        );
    }

    @Transactional
    public MyReportUpdateResponse updateMyReport(
        Long userId,
        Long reportId,
        MyReportUpdateRequest request) {
        log.info("Update my report. userId={}, reportId={}", userId, reportId);

        Report report = reportRepository.findByIdAndUser_Id(reportId, userId)
            .orElseThrow(() -> new CustomException("제보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
        validateUpdatableReport(report);

        ReportCategory category = getReportCategory(request.categoryId());
        report.update(
            request.title(),
            convertContents(request.contents()),
            request.visibility(),
            category
        );
        reportRepository.flush();

        return MyReportUpdateResponse.from(report, objectMapper);
    }

    @Transactional
    public MyReportDeleteResponse deleteMyReport(Long userId, Long reportId) {
        log.info("Delete my report. userId={}, reportId={}", userId, reportId);

        Report report = reportRepository.findByIdAndUser_Id(reportId, userId)
            .orElseThrow(() -> new CustomException("?쒕낫瑜?李얠쓣 ???놁뒿?덈떎.", ErrorCode.NOT_FOUND));
        List<ReportImage> reportImages = reportImageRepository.findByReport_IdOrderBySortOrderAsc(reportId);
        IssueGroup issueGroup = report.getIssueGroup();

        issueGroup.decreaseReportCount();
        MyReportDeleteResponse response = MyReportDeleteResponse.from(
            report,
            reportImages,
            issueGroup,
            objectMapper
        );

        reportRepository.delete(report);
        reportRepository.flush();

        return response;
    }

    @Transactional
    public ReportReactionResponse saveReportReaction(
        Long userId,
        Long reportId,
        ReportReactionRequest request) {
        log.info(
            "Save report reaction. userId={}, reportId={}, reactionType={}",
            userId,
            reportId,
            request.reactionType()
        );

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                ErrorCode.USER_NOT_FOUND));
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new CustomException("?쒕낫瑜?李얠쓣 ???놁뒿?덈떎.", ErrorCode.NOT_FOUND));

        IssueGroup issueGroup = report.getIssueGroup();

        ReportReactionLog reactionLog = reportReactionLogRepository
            .findByReport_IdAndUser_Id(reportId, userId)
            .map(existingReactionLog -> {
                ReportReactionType previousReactionType =
                    existingReactionLog.updateReactionType(request.reactionType());
                issueGroup.applyReaction(previousReactionType, request.reactionType());
                return existingReactionLog;
            })
            .orElseGet(() -> {
                ReportReactionLog newReactionLog = ReportReactionLog.create(
                    report,
                    user,
                    request.reactionType()
                );
                issueGroup.applyReaction(null, request.reactionType());
                return reportReactionLogRepository.save(newReactionLog);
            });

        issueGroupRepository.saveAndFlush(issueGroup);
        reportReactionLogRepository.flush();

        return ReportReactionResponse.from(reactionLog, report, objectMapper);
    }

    private LocalDateTime parseFromDateTime(String value) {
        return parseDateTime(value, LocalTime.MIN);
    }

    private LocalDateTime parseToDateTime(String value) {
        return parseDateTime(value, LocalTime.MAX);
    }

    private LocalDateTime parseDateTime(String value, LocalTime defaultTime) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(value).atTime(defaultTime);
            } catch (DateTimeParseException ignored) {
                throw new CustomException("날짜 형식이 올바르지 않습니다.", ErrorCode.INVALID_FORMAT);
            }
        }
    }

    private void validateDateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new CustomException("조회 시작일은 종료일보다 늦을 수 없습니다.", ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateUpdatableReport(Report report) {
        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new CustomException("접수 이후 상태의 제보는 수정할 수 없습니다.", ErrorCode.FORBIDDEN);
        }
    }

    private ReportCategory getReportCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        return reportCategoryRepository.findWithDepartmentById(categoryId)
            .orElseThrow(() -> new CustomException("카테고리를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
    }

    private String convertContents(JsonNode contents) {
        if (contents == null) {
            return null;
        }
        if (!contents.isObject()) {
            throw new CustomException("제보 본문은 JSON 객체여야 합니다.", ErrorCode.INVALID_FORMAT);
        }

        try {
            return objectMapper.writeValueAsString(contents);
        } catch (JsonProcessingException e) {
            throw new CustomException("제보 본문 형식이 올바르지 않습니다.", ErrorCode.INVALID_FORMAT);
        }
    }

    private Map<Long, List<ReportImage>> getReportImages(List<Report> reports) {
        List<Long> reportIds = reports.stream()
            .map(Report::getId)
            .toList();

        if (reportIds.isEmpty()) {
            return Map.of();
        }

        return reportImageRepository.findByReport_IdInOrderByReport_IdAscSortOrderAsc(reportIds)
            .stream()
            .collect(Collectors.groupingBy(reportImage -> reportImage.getReport().getId()));
    }

    private void validateAuthenticatedUser(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED.getMessage(), ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateReportDraftRequest(ReportDraftRequest request) {
        if (request.images() == null || request.images().isEmpty()) {
            return;
        }
        if (request.images().size() > MAX_REPORT_DRAFT_IMAGE_COUNT) {
            throw new CustomException("제보 이미지는 최대 5장까지 첨부할 수 있습니다.", ErrorCode.INVALID_PARAMETER);
        }
        request.images().stream()
            .filter(image -> image != null && !image.isEmpty())
            .forEach(image -> {
                if (image.getSize() > MAX_REPORT_DRAFT_IMAGE_SIZE) {
                    throw new CustomException("제보 이미지는 장당 50MB 이하만 첨부할 수 있습니다.", ErrorCode.INVALID_PARAMETER);
                }
                String contentType = image.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new CustomException("제보 이미지는 image/* 형식만 첨부할 수 있습니다.", ErrorCode.INVALID_FORMAT);
                }
            });
    }

    private ReportCategory resolveReportCategory(ReportAiAnalyzeResponse aiResponse) {
        String categoryCode = aiResponse.category() == null ? null : aiResponse.category().categoryCode();
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new CustomException("AI가 제보 카테고리를 분류하지 못했습니다.", ErrorCode.AI_SERVER_RESPONSE_ERROR);
        }

        return reportCategoryRepository.findByCategoryCode(categoryCode)
            .orElseThrow(() -> new CustomException("분류된 제보 카테고리를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
    }

    private ReportDraftResponse createReportDraftResponse(
        Long userId,
        ReportDraftRequest request,
        LocalDateTime occurredAt,
        AddressSnapshot address,
        ReportAiAnalyzeResponse aiResponse,
        ReportCategory category) {
        LocalDateTime resolvedOccurredAt = aiResponse.occurredAt() == null ? occurredAt : aiResponse.occurredAt();

        return new ReportDraftResponse(
            null,
            aiResponse.title(),
            resolveContents(aiResponse, request.content(), resolvedOccurredAt),
            request.latitude(),
            request.longitude(),
            address.roadAddress(),
            address.jibunAddress(),
            address.sido(),
            address.sigungu(),
            address.eupmyeondong(),
            resolvedOccurredAt,
            aiResponse.riskScore(),
            ReportStatus.SUBMITTED,
            false,
            ReportVisibility.PUBLIC,
            false,
            null,
            null,
            userId,
            category.getId(),
            null,
            category.getDepartment().getId()
        );
    }

    private JsonNode resolveContents(
        ReportAiAnalyzeResponse aiResponse,
        String originalContent,
        LocalDateTime occurredAt) {
        if (aiResponse.contents() != null && aiResponse.contents().isObject()) {
            return aiResponse.contents();
        }

        return JsonNodeFactory.instance.objectNode()
            .put("who", "확인되지 않음")
            .put("when", occurredAt.toString())
            .put("where", ADDRESS_NOT_RESOLVED)
            .put("what", originalContent)
            .put("how", originalContent)
            .put("why", "담당 기관 확인이 필요합니다.")
            .put("summary", originalContent);
    }

    private AddressSnapshot resolveAddressSnapshot(BigDecimal latitude, BigDecimal longitude) {
        String coordinateText = latitude.setScale(7, RoundingMode.HALF_UP)
            + ", "
            + longitude.setScale(7, RoundingMode.HALF_UP);
        return new AddressSnapshot(
            ADDRESS_NOT_RESOLVED + " (" + coordinateText + ")",
            ADDRESS_NOT_RESOLVED,
            ADDRESS_NOT_RESOLVED,
            ADDRESS_NOT_RESOLVED,
            ADDRESS_NOT_RESOLVED
        );
    }

    private record AddressSnapshot(
        String roadAddress,
        String jibunAddress,
        String sido,
        String sigungu,
        String eupmyeondong
    ) {
    }
}
