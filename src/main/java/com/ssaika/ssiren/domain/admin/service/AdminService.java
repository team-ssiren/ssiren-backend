package com.ssaika.ssiren.domain.admin.service;

import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueGroupMergeRequest;
import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueGroupStatusUpdateRequest;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueGroupMergeResponse;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueGroupStatusUpdateResponse;
import com.ssaika.ssiren.domain.notification.service.NotificationService;
import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportCategoryMergeRule;
import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import com.ssaika.ssiren.domain.report.repository.IssueGroupRepository;
import com.ssaika.ssiren.domain.report.repository.ReportCategoryMergeRuleRepository;
import com.ssaika.ssiren.domain.report.repository.ReportRepository;
import com.ssaika.ssiren.domain.report.repository.ReportStatusHistoryRepository;
import com.ssaika.ssiren.domain.report.service.IssueGroupStatsService;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.repository.OfficerDepartmentRepository;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.enums.IssueGroupStatus;
import com.ssaika.ssiren.global.enums.NotificationType;
import com.ssaika.ssiren.global.enums.ReportStatus;
import com.ssaika.ssiren.global.enums.UserRole;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final OfficerDepartmentRepository officerDepartmentRepository;
    private final IssueGroupRepository issueGroupRepository;
    private final ReportStatusHistoryRepository reportStatusHistoryRepository;
    private final ReportCategoryMergeRuleRepository reportCategoryMergeRuleRepository;
    private final NotificationService notificationService;
    private final IssueGroupStatsService issueGroupStatsService;

    @Transactional
    public AdminIssueGroupStatusUpdateResponse updateAdminIssueGroupStatus(
            Long userId,
            Long issueGroupId,
            AdminIssueGroupStatusUpdateRequest request
    ) {
        // 유저
        validateAuthenticatedUser(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                        ErrorCode.USER_NOT_FOUND));

        // 권한 체크
        validateAdminOrOfficer(user);

        // 이슈 그룹
        validateAdminUpdatableReportStatus(request.status());
        IssueGroup issueGroup = issueGroupRepository.findById(issueGroupId)
                .orElseThrow(() -> new CustomException("이슈 그룹을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

        // 산하 리포트들 찾기
        List<Report> reports = reportRepository.findReportsForAdminStatusUpdate(issueGroupId);

        if (reports.isEmpty()) {
            throw new CustomException("상태를 변경할 제보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND);
        }

        Report representativeReport = findRepresentativeReport(reports);
        validateIssueAccessByAgencyType(user, representativeReport);

        IssueGroupStatus nextIssueGroupStatus = toIssueGroupStatus(request.status());

        List<ReportStatusHistory> statusHistories = new ArrayList<>();
        List<Long> changedReportIds = new ArrayList<>();

        // 변경
        for (Report report : reports) {
            if (report.getStatus() == request.status()) {
                continue;
            }

            ReportStatus previousStatus = report.updateStatus(request.status());
            changedReportIds.add(report.getId());

            statusHistories.add(ReportStatusHistory.create(
                    previousStatus,
                    request.status(),
                    request.reason(),
                    report,
                    user,
                    report.getDepartment()
            ));

            if (request.shouldNotifyReporter()) {
                // 푸시 알림 전송
                sendReportStatusChangedPush(
                        report,
                        issueGroup,
                        previousStatus,
                        request.status(),
                        request.reason()
                );
            }
        }

        issueGroup.updateStatus(nextIssueGroupStatus);

        if (!statusHistories.isEmpty()) {
            reportStatusHistoryRepository.saveAll(statusHistories);
        }

        return AdminIssueGroupStatusUpdateResponse.of(
                issueGroup.getId(),
                issueGroup.getStatus(),
                request.status(),
                changedReportIds
        );
    }

    @Transactional
    public AdminIssueGroupMergeResponse mergeAdminIssueGroup(
            Long userId,
            Long targetIssueGroupId,
            AdminIssueGroupMergeRequest request
    ) {
        validateAuthenticatedUser(userId);
        validateMergeRequest(targetIssueGroupId, request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                        ErrorCode.USER_NOT_FOUND));
        validateOfficer(user);

        Set<Long> issueGroupIds = new HashSet<>();
        issueGroupIds.add(targetIssueGroupId);
        issueGroupIds.addAll(request.sourceIssueGroupIds());

        Map<Long, IssueGroup> issueGroupMap = issueGroupRepository.findAllByIdIn(issueGroupIds)
                .stream()
                .collect(Collectors.toMap(IssueGroup::getId, Function.identity()));

        if (issueGroupMap.size() != issueGroupIds.size()) {
            throw new CustomException("병합 대상 이슈 그룹을 찾을 수 없습니다.", ErrorCode.NOT_FOUND);
        }

        IssueGroup targetIssueGroup = issueGroupMap.get(targetIssueGroupId);

        List<Report> allReports = reportRepository.findByIssueGroup_IdIn(issueGroupIds);
        Map<Long, List<Report>> reportsByIssueGroup = allReports.stream()
                .collect(Collectors.groupingBy(report -> report.getIssueGroup().getId()));

        List<Report> targetReports = reportsByIssueGroup.getOrDefault(targetIssueGroupId, List.of());
        if (targetReports.isEmpty()) {
            throw new CustomException("target 이슈 그룹에 속한 제보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND);
        }

        Report targetRepresentativeReport = findRepresentativeReport(targetReports);
        validateIssueAccessByAgencyType(user, targetRepresentativeReport);

        List<Report> sourceReports = request.sourceIssueGroupIds().stream()
                .flatMap(sourceIssueGroupId -> reportsByIssueGroup
                        .getOrDefault(sourceIssueGroupId, List.of())
                        .stream())
                .toList();

        if (sourceReports.isEmpty()) {
            throw new CustomException("source 이슈 그룹에 속한 제보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND);
        }

        validateSourceIssueGroups(
                user,
                targetRepresentativeReport,
                request.sourceIssueGroupIds(),
                reportsByIssueGroup
        );

        List<Report> mergedActiveReports = Stream.concat(targetReports.stream(), sourceReports.stream())
                .filter(report -> !Boolean.TRUE.equals(report.getIsDeleted()))
                .toList();

        if (mergedActiveReports.isEmpty()) {
            throw new CustomException("병합 후 유효한 제보가 없습니다.", ErrorCode.BAD_REQUEST);
        }

        ReportCategoryMergeRule mergeRule = reportCategoryMergeRuleRepository
                .findByCategory_Id(targetRepresentativeReport.getCategory().getId())
                .orElseThrow(() -> new CustomException("카테고리 병합 기준을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

        BigDecimal newGroupDiameterMeters = issueGroupStatsService.calculateGroupDiameter(mergedActiveReports);
        validateGroupDiameter(newGroupDiameterMeters, mergeRule);

        List<ReportStatusHistory> histories = new ArrayList<>();
        List<Long> movedReportIds = new ArrayList<>();

        for (Report sourceReport : sourceReports) {
            ReportStatus currentStatus = sourceReport.getStatus();

            sourceReport.changeIssueGroup(targetIssueGroup);
            sourceReport.unmarkRepresentative();

            movedReportIds.add(sourceReport.getId());
            histories.add(ReportStatusHistory.create(
                    currentStatus,
                    currentStatus,
                    buildMergeReason(targetIssueGroupId, request.reason()),
                    sourceReport,
                    user,
                    sourceReport.getDepartment()
            ));
        }

        targetRepresentativeReport.markRepresentative();
        issueGroupStatsService.refreshIssueGroupByReports(
                targetIssueGroup,
                mergedActiveReports,
                targetRepresentativeReport
        );

        reportStatusHistoryRepository.saveAll(histories);

        // 알림이 필요하면 기존 notificationService.sendPushToUser(...)를 여기서 호출
        // if (request.shouldNotifyReporter()) {
        // }

        return AdminIssueGroupMergeResponse.of(
                targetIssueGroup.getId(),
                request.sourceIssueGroupIds(),
                movedReportIds,
                targetIssueGroup.getReportCount(),
                targetIssueGroup.getGroupDiameterMeters()
        );
    }

    private void validateAuthenticatedUser(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED.getMessage(), ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateAdminOrOfficer(User user) {
        if (user.getRole() != UserRole.ADMIN && user.getRole() != UserRole.OFFICER) {
            throw new CustomException(ErrorCode.FORBIDDEN.getMessage(), ErrorCode.FORBIDDEN);
        }
    }

    private void validateAdminUpdatableReportStatus(ReportStatus status) {
        if (status == null) {
            throw new CustomException("변경할 상태는 필수입니다.", ErrorCode.MISSING_PARAMETER);
        }

        if (status == ReportStatus.RECEIVED
                || status == ReportStatus.CHECKING
                || status == ReportStatus.IN_PROGRESS
                || status == ReportStatus.COMPLETED
                || status == ReportStatus.REJECTED) {
            return;
        }

        throw new CustomException(
                "상태 변경 API에서는 RECEIVED, CHECKING, IN_PROGRESS, COMPLETED, REJECTED만 사용할 수 있습니다.",
                ErrorCode.INVALID_PARAMETER
        );
    }

    private IssueGroupStatus toIssueGroupStatus(ReportStatus status) {
        return switch (status) {
            case RECEIVED, CHECKING, IN_PROGRESS -> IssueGroupStatus.ACTIVE;
            case COMPLETED -> IssueGroupStatus.RESOLVED;
            case REJECTED -> IssueGroupStatus.REJECTED;
            default -> throw new CustomException("이슈 그룹 상태로 변환할 수 없는 제보 상태입니다.",
                    ErrorCode.INVALID_PARAMETER);
        };
    }

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

    // 푸시 알림 부분
    private void sendReportStatusChangedPush(
            Report report,
            IssueGroup issueGroup,
            ReportStatus previousStatus,
            ReportStatus newStatus,
            String reason
    ) {
        try {
            notificationService.sendPushToUser(
                    report.getUser().getId(),
                    buildReportStatusChangedPushTitle(report),
                    buildReportStatusChangedPushBody(report, previousStatus, newStatus),
                    Map.of(
                            "type", NotificationType.REPORT_STATUS_CHANGED.name(),
                            "issueGroupId", String.valueOf(issueGroup.getId()),
                            "reportId", String.valueOf(report.getId()),
                            "previousStatus", previousStatus.name(),
                            "newStatus", newStatus.name(),
                            "previousStatusLabel", toReportStatusLabel(previousStatus),
                            "newStatusLabel", toReportStatusLabel(newStatus),
                            "reportTitle", report.getTitle(),
                            "reason", reason
                    )
            );
        } catch (RuntimeException e) {
            log.warn(
                    "Failed to send report status changed push. reportId={}, userId={}, previousStatus={}, newStatus={}",
                    report.getId(),
                    report.getUser().getId(),
                    previousStatus,
                    newStatus,
                    e
            );
        }
    }

    private String buildReportStatusChangedPushTitle(Report report) {
        return report.getUser().getNickname() + "님! 제보 상태가 변경됐어요.";
    }

    private String buildReportStatusChangedPushBody(
            Report report,
            ReportStatus previousStatus,
            ReportStatus newStatus
    ) {
        return "'%s' 제보가 %s에서 %s로 변경됐어요."
                .formatted(
                        report.getTitle(),
                        toReportStatusLabel(previousStatus),
                        toReportStatusLabel(newStatus)
                );
    }

    private String toReportStatusLabel(ReportStatus status) {
        return switch (status) {
            case SUBMITTED -> "접수 전";
            case RECEIVED -> "접수 완료";
            case CHECKING -> "확인 중";
            case IN_PROGRESS -> "처리 중";
            case COMPLETED -> "처리 완료";
            case REJECTED -> "반려";
            case TRANSFERRED -> "이관";
            case MERGED -> "병합";
        };
    }

    private void validateMergeRequest(Long targetIssueGroupId, AdminIssueGroupMergeRequest request) {
        if (targetIssueGroupId == null) {
            throw new CustomException("target 이슈 그룹 ID는 필수입니다.", ErrorCode.MISSING_PARAMETER);
        }
        if (request.sourceIssueGroupIds() == null || request.sourceIssueGroupIds().isEmpty()) {
            throw new CustomException("source 이슈 그룹 ID 목록은 필수입니다.", ErrorCode.MISSING_PARAMETER);
        }
        if (request.sourceIssueGroupIds().contains(targetIssueGroupId)) {
            throw new CustomException("target 이슈 그룹은 source 목록에 포함될 수 없습니다.", ErrorCode.INVALID_PARAMETER);
        }
        if (request.sourceIssueGroupIds().stream().distinct().count() != request.sourceIssueGroupIds().size()) {
            throw new CustomException("source 이슈 그룹 ID가 중복되었습니다.", ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateOfficer(User user) {
        if (user.getRole() != UserRole.OFFICER) {
            throw new CustomException(ErrorCode.FORBIDDEN.getMessage(), ErrorCode.FORBIDDEN);
        }
    }

    private void validateSourceIssueGroups(
            User user,
            Report targetRepresentativeReport,
            List<Long> sourceIssueGroupIds,
            Map<Long, List<Report>> reportsByIssueGroup
    ) {
        for (Long sourceIssueGroupId : sourceIssueGroupIds) {
            List<Report> sourceReports = reportsByIssueGroup.getOrDefault(sourceIssueGroupId, List.of());
            if (sourceReports.isEmpty()) {
                throw new CustomException("source 이슈 그룹에 속한 제보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND);
            }

            Report sourceRepresentativeReport = findRepresentativeReport(sourceReports);
            validateIssueAccessByAgencyType(user, sourceRepresentativeReport);
            validateSameCategory(targetRepresentativeReport, sourceRepresentativeReport);
            validateSameDepartment(targetRepresentativeReport, sourceRepresentativeReport);
            validateMergeableIssueGroupStatus(sourceRepresentativeReport.getIssueGroup());
            validateMergeableReportStatuses(sourceReports);
        }

        validateMergeableIssueGroupStatus(targetRepresentativeReport.getIssueGroup());
        validateMergeableReportStatuses(reportsByIssueGroup.getOrDefault(
                targetRepresentativeReport.getIssueGroup().getId(),
                List.of()
        ));
    }

    private void validateSameCategory(Report targetRepresentativeReport, Report sourceRepresentativeReport) {
        Long targetCategoryId = targetRepresentativeReport.getCategory().getId();
        Long sourceCategoryId = sourceRepresentativeReport.getCategory().getId();

        if (!Objects.equals(targetCategoryId, sourceCategoryId)) {
            throw new CustomException("같은 자식 카테고리의 이슈 그룹만 병합할 수 있습니다.", ErrorCode.BAD_REQUEST);
        }
    }

    private void validateSameDepartment(Report targetRepresentativeReport, Report sourceRepresentativeReport) {
        Long targetDepartmentId = targetRepresentativeReport.getDepartment().getId();
        Long sourceDepartmentId = sourceRepresentativeReport.getDepartment().getId();

        if (!Objects.equals(targetDepartmentId, sourceDepartmentId)) {
            throw new CustomException("같은 담당 부서의 이슈 그룹만 병합할 수 있습니다.", ErrorCode.BAD_REQUEST);
        }
    }

    private void validateMergeableIssueGroupStatus(IssueGroup issueGroup) {
        if (issueGroup.getStatus() == IssueGroupStatus.RESOLVED
                || issueGroup.getStatus() == IssueGroupStatus.REJECTED) {
            throw new CustomException("완료 또는 반려된 이슈 그룹은 병합할 수 없습니다.", ErrorCode.BAD_REQUEST);
        }
    }

    private void validateMergeableReportStatuses(List<Report> reports) {
        boolean hasNotMergeableStatus = reports.stream()
                .filter(report -> !Boolean.TRUE.equals(report.getIsDeleted()))
                .map(Report::getStatus)
                .anyMatch(status -> status == ReportStatus.COMPLETED
                        || status == ReportStatus.REJECTED
                        || status == ReportStatus.TRANSFERRED
                        || status == ReportStatus.MERGED);

        if (hasNotMergeableStatus) {
            throw new CustomException(
                    "완료, 반려, 이관, 병합 상태의 제보가 포함된 이슈 그룹은 병합할 수 없습니다.",
                    ErrorCode.BAD_REQUEST
            );
        }
    }

    private void validateGroupDiameter(
            BigDecimal newGroupDiameterMeters,
            ReportCategoryMergeRule mergeRule
    ) {
        BigDecimal maxGroupDiameterMeters = BigDecimal.valueOf(mergeRule.getMaxGroupDiameterMeters());

        if (newGroupDiameterMeters.compareTo(maxGroupDiameterMeters) > 0) {
            throw new CustomException(
                    "병합 후 이슈 그룹 범위가 카테고리 병합 기준을 초과합니다.",
                    ErrorCode.BAD_REQUEST
            );
        }
    }

    private String buildMergeReason(Long targetIssueGroupId, String reason) {
        return "수동 병합: targetIssueGroupId=" + targetIssueGroupId + ", reason=" + reason;
    }

}
