package com.ssaika.ssiren.domain.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ssaika.ssiren.domain.report.dto.response.MyReportDetailResponse;
import com.ssaika.ssiren.domain.report.dto.response.MyReportResponse;
import com.ssaika.ssiren.domain.report.dto.request.MyReportUpdateRequest;
import com.ssaika.ssiren.domain.report.dto.request.ReportReactionRequest;
import com.ssaika.ssiren.domain.report.dto.response.IssueDetailResponse;
import com.ssaika.ssiren.domain.report.dto.response.IssueResponse;
import com.ssaika.ssiren.domain.report.dto.response.MyReportDeleteResponse;
import com.ssaika.ssiren.domain.report.dto.response.MyReportUpdateResponse;
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
import com.ssaika.ssiren.global.enums.IssueGroupStatus;
import com.ssaika.ssiren.global.enums.ReportStatus;
import com.ssaika.ssiren.global.enums.ReportVisibility;
import com.ssaika.ssiren.global.enums.ReportReactionType;
import com.ssaika.ssiren.domain.report.dto.response.ReportListResponse;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final IssueGroupRepository issueGroupRepository;
    private final ReportCategoryRepository reportCategoryRepository;
    private final ReportImageRepository reportImageRepository;
    private final ReportStatusHistoryRepository reportStatusHistoryRepository;
    private final ReportReactionLogRepository reportReactionLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public List<IssueResponse> getIssues(
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng,
        Long categoryId,
        Long agencyId,
        IssueGroupStatus status,
        BigDecimal riskMin,
        BigDecimal riskMax,
        String from,
        String to) {
        validateRadiusParameters(latitude, longitude, radiusMeters);
        validateBoundsParameters(swLat, swLng, neLat, neLng);
        validateRiskRange(riskMin, riskMax);
        validateBoundsRange(swLat, swLng, neLat, neLng);
        LocalDateTime fromDateTime = parseFromDateTime(from);
        LocalDateTime toDateTime = parseToDateTime(to);
        validateDateRange(fromDateTime, toDateTime);
        log.info(
            "Get issues. latitude={}, longitude={}, radiusMeters={}, swLat={}, swLng={}, "
                + "neLat={}, neLng={}, categoryId={}, agencyId={}, status={}, riskMin={}, "
                + "riskMax={}, from={}, to={}",
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
            fromDateTime,
            toDateTime
        );

        Specification<Report> specification = ReportSpecification.isRepresentative()
            .and(ReportSpecification.hasVisibility(ReportVisibility.PUBLIC))
            .and(ReportSpecification.isNotDeleted())
            .and(ReportSpecification.hasCategory(categoryId))
            .and(ReportSpecification.hasAgencyType(agencyId))
            .and(ReportSpecification.hasIssueGroupStatus(status))
            .and(ReportSpecification.issueGroupRiskScoreFrom(riskMin))
            .and(ReportSpecification.issueGroupRiskScoreTo(riskMax))
            .and(ReportSpecification.recentReportedAtFrom(fromDateTime))
            .and(ReportSpecification.recentReportedAtTo(toDateTime))
            .and(ReportSpecification.issueGroupInBounds(swLat, swLng, neLat, neLng))
            .and(ReportSpecification.issueGroupWithinRadius(latitude, longitude, radiusMeters));

        return reportRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "issueGroup.riskScore"))
            .stream()
            .map(report -> IssueResponse.from(report, objectMapper))
            .toList();
    }

    public IssueDetailResponse getIssue(Long issueGroupId) {
        log.info("Get issue detail. issueGroupId={}", issueGroupId);

        IssueGroup issueGroup = issueGroupRepository.findById(issueGroupId)
            .orElseThrow(() -> new CustomException("?댁뒋 洹몃９???李얠쓣 ???놁뒿?덈떎.", ErrorCode.NOT_FOUND));
        Specification<Report> specification = ReportSpecification.hasIssueGroup(issueGroupId)
            .and(ReportSpecification.hasVisibility(ReportVisibility.PUBLIC))
            .and(ReportSpecification.isNotDeleted());
        List<Report> reports = reportRepository.findAll(
            specification,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Report representativeReport = reports.stream()
            .filter(Report::getIsRepresentative)
            .findFirst()
            .orElseGet(() -> reports.isEmpty() ? null : reports.get(0));

        return IssueDetailResponse.from(issueGroup, representativeReport, reports, objectMapper);
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

    private void validateRadiusParameters(
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters) {
        boolean hasAnyRadiusParameter =
            latitude != null || longitude != null || radiusMeters != null;
        boolean hasAllRadiusParameters =
            latitude != null && longitude != null && radiusMeters != null;
        if (hasAnyRadiusParameter && !hasAllRadiusParameters) {
            throw new CustomException("諛섍꼍 議고쉶 ?뚮씪誘명꽣媛 遺議깊빀?덈떎.", ErrorCode.MISSING_PARAMETER);
        }
        if (radiusMeters != null && radiusMeters <= 0) {
            throw new CustomException("諛섍꼍???양닔?ъ빞 ?⑸땲??", ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateBoundsParameters(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng) {
        boolean hasAnyBoundsParameter =
            swLat != null || swLng != null || neLat != null || neLng != null;
        boolean hasAllBoundsParameters =
            swLat != null && swLng != null && neLat != null && neLng != null;
        if (hasAnyBoundsParameter && !hasAllBoundsParameters) {
            throw new CustomException("吏???곸뿭 議고쉶 ?뚮씪誘명꽣媛 遺議깊빀?덈떎.", ErrorCode.MISSING_PARAMETER);
        }
    }

    private void validateRiskRange(BigDecimal riskMin, BigDecimal riskMax) {
        if (riskMin != null && riskMax != null && riskMin.compareTo(riskMax) > 0) {
            throw new CustomException("理쒖냼 ?꾪뿕 ?먯닔媛 理쒕? ?꾪뿕 ?먯닔蹂대떎 ?대쓣 ???놁뒿?덈떎.",
                ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateBoundsRange(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng) {
        if (swLat == null || swLng == null || neLat == null || neLng == null) {
            return;
        }
        if (swLat.compareTo(neLat) > 0 || swLng.compareTo(neLng) > 0) {
            throw new CustomException("吏???곸뿭 踰붿쐞媛 ?щ컮瑜댁? ?딆뒿?덈떎.", ErrorCode.INVALID_PARAMETER);
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
}
