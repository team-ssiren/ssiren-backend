package com.ssaika.ssiren.domain.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.ssaika.ssiren.domain.report.dto.response.MyReportDetailResponse;
import com.ssaika.ssiren.domain.report.dto.response.MyReportResponse;
import com.ssaika.ssiren.domain.report.dto.request.MyReportUpdateRequest;
import com.ssaika.ssiren.domain.report.dto.response.MyReportDeleteResponse;
import com.ssaika.ssiren.domain.report.dto.response.MyReportUpdateResponse;
import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportCategory;
import com.ssaika.ssiren.domain.report.entity.ReportImage;
import com.ssaika.ssiren.domain.report.entity.ReportReactionLog;
import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import com.ssaika.ssiren.domain.report.repository.ReportCategoryRepository;
import com.ssaika.ssiren.domain.report.repository.ReportImageRepository;
import com.ssaika.ssiren.domain.report.repository.ReportReactionLogRepository;
import com.ssaika.ssiren.domain.report.repository.ReportRepository;
import com.ssaika.ssiren.domain.report.repository.ReportSpecification;
import com.ssaika.ssiren.domain.report.repository.ReportStatusHistoryRepository;
import com.ssaika.ssiren.global.enums.ReportStatus;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportRepository reportRepository;
    private final ReportCategoryRepository reportCategoryRepository;
    private final ReportImageRepository reportImageRepository;
    private final ReportStatusHistoryRepository reportStatusHistoryRepository;
    private final ReportReactionLogRepository reportReactionLogRepository;
    private final ObjectMapper objectMapper;

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
}
