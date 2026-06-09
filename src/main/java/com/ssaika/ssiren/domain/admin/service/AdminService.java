package com.ssaika.ssiren.domain.admin.service;

import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueGroupStatusUpdateRequest;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueGroupStatusUpdateResponse;
import com.ssaika.ssiren.domain.notification.service.NotificationService;
import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import com.ssaika.ssiren.domain.report.repository.IssueGroupRepository;
import com.ssaika.ssiren.domain.report.repository.ReportRepository;
import com.ssaika.ssiren.domain.report.repository.ReportStatusHistoryRepository;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final NotificationService notificationService;

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
                    user
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
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }

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
        notificationService.sendPushToUser(
                report.getUser().getId(),
                "제보 처리 상태가 변경되었습니다.",
                buildReportStatusChangedPushBody(newStatus),
                Map.of(
                        "type", NotificationType.REPORT_STATUS_CHANGED.name(),
                        "issueGroupId", String.valueOf(issueGroup.getId()),
                        "reportId", String.valueOf(report.getId()),
                        "previousStatus", previousStatus.name(),
                        "newStatus", newStatus.name(),
                        "reason", reason
                )
        );
    }

    private String buildReportStatusChangedPushBody(ReportStatus status) {
        return switch (status) {
            case RECEIVED -> "제보가 접수되었습니다.";
            case CHECKING -> "제보 내용을 확인 중입니다.";
            case IN_PROGRESS -> "제보 처리가 진행 중입니다.";
            case COMPLETED -> "제보 처리가 완료되었습니다.";
            case REJECTED -> "제보가 반려되었습니다.";
            default -> "제보 처리 상태가 변경되었습니다.";
        };
    }
}