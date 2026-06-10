package com.ssaika.ssiren.domain.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueSearchRequest;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueDetailResponse;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueResponse;
import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportImage;
import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import com.ssaika.ssiren.domain.report.repository.*;
import com.ssaika.ssiren.domain.user.entity.OfficerDepartment;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.repository.OfficerDepartmentRepository;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.enums.UserRole;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMapService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final OfficerDepartmentRepository officerDepartmentRepository;
    private final IssueGroupRepository issueGroupRepository;
    private final ReportImageRepository reportImageRepository;
    private final ReportStatusHistoryRepository reportStatusHistoryRepository;
    private final ObjectMapper objectMapper;

    public List<AdminIssueResponse> getAdminIssues(
            Long userId,
            AdminIssueSearchRequest request
    ) {
        validateAuthenticatedUser(userId);
        validateRadiusParameters(request.latitude(), request.longitude(), request.radiusMeters());
        validateBoundsParameters(request.swLat(), request.swLng(), request.neLat(), request.neLng());
        validateRiskRange(request.riskMin(), request.riskMax());
        validateBoundsRange(request.swLat(), request.swLng(), request.neLat(), request.neLng());

        LocalDateTime fromDateTime = parseFromDateTime(request.from());
        LocalDateTime toDateTime = parseToDateTime(request.to());
        validateDateRange(fromDateTime, toDateTime);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                        ErrorCode.USER_NOT_FOUND));

        validateOfficer(user);

        log.info(
                "Get officer issues. userId={}, role={}, keyword={}, sort={}, reportStatus={}, "
                        + "status={}, categoryId={}, agencyTypeId={}, departmentId={}, myDepartmentOnly={}",
                userId,
                user.getRole(),
                request.keyword(),
                request.resolvedSort(),
                request.reportStatus(),
                request.status(),
                request.categoryId(),
                request.agencyTypeId(),
                request.departmentId(),
                request.myDepartmentOnly()
        );

        Specification<Report> specification = ReportSpecification.isRepresentative()
                .and(ReportSpecification.isDeletedOnly(request.deletedOnly()))
                .and(ReportSpecification.hasKeyword(request.keyword()))
                .and(ReportSpecification.hasCategory(request.categoryId()))
                .and(ReportSpecification.hasIssueGroupStatus(request.status()))
                .and(ReportSpecification.hasStatus(request.reportStatus()))
                .and(ReportSpecification.issueGroupRiskScoreFrom(request.riskMin()))
                .and(ReportSpecification.issueGroupRiskScoreTo(request.riskMax()))
                .and(ReportSpecification.recentReportedAtFrom(fromDateTime))
                .and(ReportSpecification.recentReportedAtTo(toDateTime))
                .and(ReportSpecification.issueGroupInBounds(
                        request.swLat(),
                        request.swLng(),
                        request.neLat(),
                        request.neLng()
                ))
                .and(ReportSpecification.issueGroupWithinRadius(
                        request.latitude(),
                        request.longitude(),
                        request.radiusMeters()
                ))
                .and(resolveJurisdictionSpecification(
                        user,
                        request.agencyTypeId(),
                        request.departmentId(),
                        request.myDepartmentOnly()
                ));

        List<Report> representativeReports = reportRepository.findAdminIssueRepresentatives(
                specification,
                request.resolvedSort()
        );

        if (representativeReports.isEmpty()) {
            return List.of();
        }

        List<Long> representativeReportIds = representativeReports.stream()
                .map(Report::getId)
                .toList();

        Map<Long, List<ReportImage>> reportImageMap = reportImageRepository
                .findByReport_IdInOrderByReport_IdAscSortOrderAsc(representativeReportIds)
                .stream()
                .collect(Collectors.groupingBy(reportImage -> reportImage.getReport().getId()));

        Map<Long, List<ReportStatusHistory>> statusHistoryMap = reportStatusHistoryRepository
                .findByReport_IdInOrderByReport_IdAscCreatedAtAsc(representativeReportIds)
                .stream()
                .collect(Collectors.groupingBy(statusHistory -> statusHistory.getReport().getId()));

        return representativeReports.stream()
                .map(report -> AdminIssueResponse.from(
                        report,
                        reportImageMap.getOrDefault(report.getId(), List.of()),
                        statusHistoryMap.getOrDefault(report.getId(), List.of()),
                        objectMapper
                ))
                .toList();
    }

    public AdminIssueDetailResponse getAdminIssue(Long userId, Long issueGroupId) {
        validateAuthenticatedUser(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                        ErrorCode.USER_NOT_FOUND));

        validateOfficer(user);

        log.info("Get officer issue detail. userId={}, role={}, issueGroupId={}",
                userId, user.getRole(), issueGroupId);

        IssueGroup issueGroup = issueGroupRepository.findById(issueGroupId)
                .orElseThrow(() -> new CustomException("이슈 그룹을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

        Specification<Report> specification = ReportSpecification.hasIssueGroup(issueGroupId);
        List<Report> reports = reportRepository.findAll(
                specification,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        if (reports.isEmpty()) {
            throw new CustomException("이슈 그룹에 속한 제보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND);
        }

        Report representativeReport = findRepresentativeReport(reports);

        validateIssueAccessByAgencyType(user, representativeReport);

        List<Long> reportIds = reports.stream()
                .map(Report::getId)
                .toList();

        Map<Long, List<ReportImage>> reportImageMap = reportImageRepository
                .findByReport_IdInOrderByReport_IdAscSortOrderAsc(reportIds)
                .stream()
                .collect(Collectors.groupingBy(reportImage -> reportImage.getReport().getId()));

        List<ReportStatusHistory> representativeStatusHistories =
                reportStatusHistoryRepository.findByReport_IdOrderByCreatedAtAsc(
                        representativeReport.getId()
                );

        return AdminIssueDetailResponse.from(
                issueGroup,
                representativeReport,
                reports,
                reportImageMap,
                representativeStatusHistories,
                objectMapper
        );
    }

    // 이슈 그룹 조회 파트
    private Specification<Report> resolveJurisdictionSpecification(
            User user,
            Long agencyTypeId,
            Long departmentId,
            Boolean myDepartmentOnly) {
        List<OfficerDepartment> officerDepartments =
                officerDepartmentRepository.findByUserId(user.getId());
        List<Long> departmentIds = officerDepartments.stream()
                .map(officerDepartment -> officerDepartment.getDepartment().getId())
                .distinct()
                .toList();
        List<Long> agencyTypeIds = officerDepartments.stream()
                .map(officerDepartment -> officerDepartment.getDepartment().getAgencyType().getId())
                .distinct()
                .toList();

        if (departmentIds.isEmpty() || agencyTypeIds.isEmpty()) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.disjunction();
        }

        if (Boolean.TRUE.equals(myDepartmentOnly)) {
            return ReportSpecification.hasDepartmentIn(departmentIds)
                    .and(ReportSpecification.hasDepartment(departmentId));
        }

        return ReportSpecification.hasAgencyTypeIn(agencyTypeIds)
                .and(ReportSpecification.hasAgencyType(agencyTypeId));
    }

    private void validateAuthenticatedUser(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED.getMessage(), ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateOfficer(User user) {
        if (user.getRole() != UserRole.OFFICER) {
            throw new CustomException(ErrorCode.FORBIDDEN.getMessage(), ErrorCode.FORBIDDEN);
        }
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
            throw new CustomException("조회 시작일은 종료일보다 이후일 수 없습니다.", ErrorCode.INVALID_PARAMETER);
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
            throw new CustomException("반경 조회에는 latitude, longitude, radiusMeters가 모두 필요합니다.",
                    ErrorCode.MISSING_PARAMETER);
        }
        if (radiusMeters != null && radiusMeters <= 0) {
            throw new CustomException("radiusMeters는 0보다 커야 합니다.", ErrorCode.INVALID_PARAMETER);
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
            throw new CustomException("지도 영역 조회에는 swLat, swLng, neLat, neLng가 모두 필요합니다.",
                    ErrorCode.MISSING_PARAMETER);
        }
    }

    private void validateRiskRange(BigDecimal riskMin, BigDecimal riskMax) {
        if (riskMin != null && riskMax != null && riskMin.compareTo(riskMax) > 0) {
            throw new CustomException("riskMin은 riskMax보다 클 수 없습니다.", ErrorCode.INVALID_PARAMETER);
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
            throw new CustomException("지도 영역 좌표 범위가 올바르지 않습니다.", ErrorCode.INVALID_PARAMETER);
        }
    }

    // 이슈 그룹 상세 조회 파트
    private Report findRepresentativeReport(List<Report> reports) {
        return reports.stream()
                .filter(report -> Boolean.TRUE.equals(report.getIsRepresentative()))
                .findFirst()
                .orElse(reports.get(0));
    }

    private void validateIssueAccessByAgencyType(User user, Report representativeReport) {
        List<Long> officerAgencyTypeIds = officerDepartmentRepository.findByUserId(user.getId())
                .stream()
                .map(officerDepartment -> officerDepartment.getDepartment().getAgencyType().getId())
                .distinct()
                .toList();

        Long issueAgencyTypeId = representativeReport.getDepartment().getAgencyType().getId();

        if (!officerAgencyTypeIds.contains(issueAgencyTypeId)) {
            throw new CustomException(ErrorCode.FORBIDDEN.getMessage(), ErrorCode.FORBIDDEN);
        }
    }

}