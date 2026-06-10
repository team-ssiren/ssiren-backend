package com.ssaika.ssiren.domain.admin.service;

import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueGroupTransferCreateRequest;
import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueGroupTransferDecisionRequest;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueGroupTransferHistoryListResponse;
import com.ssaika.ssiren.domain.admin.dto.response.AdminIssueGroupTransferHistoryResponse;
import com.ssaika.ssiren.domain.agency.entity.Department;
import com.ssaika.ssiren.domain.agency.repository.DepartmentRepository;
import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.domain.report.entity.IssueGroupTransferHistory;
import com.ssaika.ssiren.domain.report.entity.IssueGroupTransferHistoryStatus;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import com.ssaika.ssiren.domain.report.repository.IssueGroupRepository;
import com.ssaika.ssiren.domain.report.repository.IssueGroupTransferHistoryRepository;
import com.ssaika.ssiren.domain.report.repository.ReportRepository;
import com.ssaika.ssiren.domain.report.repository.ReportStatusHistoryRepository;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.repository.OfficerDepartmentRepository;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
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
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDepartmentTransferService {

    private final DepartmentRepository departmentRepository;
    private final IssueGroupTransferHistoryRepository issueGroupTransferHistoryRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final OfficerDepartmentRepository officerDepartmentRepository;
    private final IssueGroupRepository issueGroupRepository;
    private final ReportStatusHistoryRepository reportStatusHistoryRepository;

    @Transactional
    public AdminIssueGroupTransferHistoryResponse createIssueGroupTransferRequest(
            Long userId,
            Long issueGroupId,
            AdminIssueGroupTransferCreateRequest request
    ) {
        validateAuthenticatedUser(userId);

        User requestUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                        ErrorCode.USER_NOT_FOUND));
        validateOfficer(requestUser);

        IssueGroup issueGroup = issueGroupRepository.findById(issueGroupId)
                .orElseThrow(() -> new CustomException("이슈 그룹을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

        List<Report> reports = reportRepository.findByIssueGroup_Id(issueGroupId);
        if (reports.isEmpty()) {
            throw new CustomException("이슈 그룹에 속한 제보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND);
        }

        Report representativeReport = findRepresentativeReport(reports);
        validateIssueAccessByAgencyType(requestUser, representativeReport);
        validateTransferableReportStatus(representativeReport.getStatus());

        Department fromDepartment = representativeReport.getDepartment();
        Department targetDepartment = departmentRepository.findById(request.targetDepartmentId())
                .orElseThrow(() -> new CustomException("이관 대상 부서를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

        validateDifferentDepartment(fromDepartment, targetDepartment);
        validateNoPendingTransferRequest(issueGroupId);

        IssueGroupTransferHistory history = IssueGroupTransferHistory.create(
                issueGroup,
                fromDepartment,
                targetDepartment,
                requestUser,
                request.requestReason()
        );

        IssueGroupTransferHistory savedHistory = issueGroupTransferHistoryRepository.save(history);

        List<ReportStatusHistory> statusHistories = reports.stream()
                .map(report -> ReportStatusHistory.create(
                        report.getStatus(),
                        ReportStatus.TRANSFERRED,
                        buildTransferRequestReason(fromDepartment, targetDepartment, request.requestReason()),
                        report,
                        requestUser
                ))
                .toList();

        reportStatusHistoryRepository.saveAll(statusHistories);

        return AdminIssueGroupTransferHistoryResponse.of(savedHistory, reports.size());
    }

    public AdminIssueGroupTransferHistoryListResponse getIncomingIssueGroupTransferRequests(Long userId) {
        validateAuthenticatedUser(userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                        ErrorCode.USER_NOT_FOUND));
        validateOfficer(user);

        List<Long> departmentIds = getOfficerDepartmentIds(user);
        if (departmentIds.isEmpty()) {
            return new AdminIssueGroupTransferHistoryListResponse(List.of());
        }

        List<IssueGroupTransferHistory> histories =
                issueGroupTransferHistoryRepository.findByTargetDepartment_IdInAndStatusOrderByCreatedAtDesc(
                        departmentIds,
                        IssueGroupTransferHistoryStatus.REQUESTED
                );

        return AdminIssueGroupTransferHistoryListResponse.from(histories);
    }

    @Transactional
    public AdminIssueGroupTransferHistoryResponse decideIssueGroupTransferRequest(
            Long userId,
            Long transferId,
            AdminIssueGroupTransferDecisionRequest request
    ) {
        if (request.isAccepted()) {
            return acceptIssueGroupTransferRequest(userId, transferId, request.responseReason());
        }
        if (request.isRejected()) {
            return rejectIssueGroupTransferRequest(userId, transferId, request.responseReason());
        }

        throw new CustomException(
                "이관 요청 응답 상태는 ACCEPTED 또는 REJECTED만 사용할 수 있습니다.",
                ErrorCode.INVALID_PARAMETER
        );
    }

    private AdminIssueGroupTransferHistoryResponse acceptIssueGroupTransferRequest(
            Long userId,
            Long transferId,
            String responseReason
    ) {
        validateAuthenticatedUser(userId);

        User responseUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                        ErrorCode.USER_NOT_FOUND));
        validateOfficer(responseUser);

        IssueGroupTransferHistory history = issueGroupTransferHistoryRepository.findWithDetailsById(transferId)
                .orElseThrow(() -> new CustomException("이관 요청을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

        validatePendingTransferRequest(history);
        validateOfficerInDepartment(responseUser, history.getTargetDepartment().getId());

        List<Report> reports = reportRepository.findByIssueGroup_Id(history.getIssueGroup().getId());
        if (reports.isEmpty()) {
            throw new CustomException("이슈 그룹에 속한 제보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND);
        }

        List<ReportStatusHistory> statusHistories = new ArrayList<>();

        for (Report report : reports) {
            ReportStatus currentStatus = report.getStatus();
            report.changeDepartment(history.getTargetDepartment());

            statusHistories.add(ReportStatusHistory.create(
                    ReportStatus.TRANSFERRED,
                    currentStatus,
                    buildTransferAcceptReason(history.getTargetDepartment(), responseReason),
                    report,
                    responseUser
            ));
        }

        history.accept(responseUser, responseReason);
        reportStatusHistoryRepository.saveAll(statusHistories);

        return AdminIssueGroupTransferHistoryResponse.of(history, reports.size());
    }

    private AdminIssueGroupTransferHistoryResponse rejectIssueGroupTransferRequest(
            Long userId,
            Long transferId,
            String responseReason
    ) {
        validateAuthenticatedUser(userId);

        User responseUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                        ErrorCode.USER_NOT_FOUND));
        validateOfficer(responseUser);

        IssueGroupTransferHistory history = issueGroupTransferHistoryRepository.findWithDetailsById(transferId)
                .orElseThrow(() -> new CustomException("이관 요청을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

        validatePendingTransferRequest(history);
        validateOfficerInDepartment(responseUser, history.getTargetDepartment().getId());

        List<Report> reports = reportRepository.findByIssueGroup_Id(history.getIssueGroup().getId());

        List<ReportStatusHistory> statusHistories = reports.stream()
                .map(report -> ReportStatusHistory.create(
                        ReportStatus.TRANSFERRED,
                        report.getStatus(),
                        buildTransferRejectReason(responseReason),
                        report,
                        responseUser
                ))
                .toList();

        history.reject(responseUser, responseReason);

        if (!statusHistories.isEmpty()) {
            reportStatusHistoryRepository.saveAll(statusHistories);
        }

        return AdminIssueGroupTransferHistoryResponse.of(history, 0);
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

    private void validateTransferableReportStatus(ReportStatus status) {
        if (status == ReportStatus.SUBMITTED
                || status == ReportStatus.RECEIVED
                || status == ReportStatus.CHECKING
                || status == ReportStatus.IN_PROGRESS) {
            return;
        }

        throw new CustomException(
                "완료, 반려, 이관, 병합 상태의 이슈 그룹은 이관 요청을 생성할 수 없습니다.",
                ErrorCode.BAD_REQUEST
        );
    }

    private void validateDifferentDepartment(Department fromDepartment, Department targetDepartment) {
        if (Objects.equals(fromDepartment.getId(), targetDepartment.getId())) {
            throw new CustomException("현재 담당 부서와 동일한 부서로는 이관할 수 없습니다.", ErrorCode.BAD_REQUEST);
        }
    }

    private void validateNoPendingTransferRequest(Long issueGroupId) {
        boolean existsPendingTransferRequest =
                issueGroupTransferHistoryRepository.existsByIssueGroup_IdAndStatus(
                        issueGroupId,
                        IssueGroupTransferHistoryStatus.REQUESTED
                );

        if (existsPendingTransferRequest) {
            throw new CustomException("이미 처리 대기 중인 이관 요청이 있습니다.", ErrorCode.BAD_REQUEST);
        }
    }

    private void validatePendingTransferRequest(IssueGroupTransferHistory history) {
        if (history.getStatus() != IssueGroupTransferHistoryStatus.REQUESTED) {
            throw new CustomException("이미 처리된 이관 요청입니다.", ErrorCode.BAD_REQUEST);
        }
    }

    private void validateOfficerInDepartment(User user, Long departmentId) {
        boolean belongsToDepartment = getOfficerDepartmentIds(user).contains(departmentId);

        if (!belongsToDepartment) {
            throw new CustomException(ErrorCode.FORBIDDEN.getMessage(), ErrorCode.FORBIDDEN);
        }
    }

    private List<Long> getOfficerDepartmentIds(User user) {
        return officerDepartmentRepository.findByUserId(user.getId())
                .stream()
                .map(officerDepartment -> officerDepartment.getDepartment().getId())
                .distinct()
                .toList();
    }

    private String buildTransferRequestReason(
            Department fromDepartment,
            Department targetDepartment,
            String requestReason
    ) {
        return "이관 요청: "
                + fromDepartment.getName()
                + " -> "
                + targetDepartment.getName()
                + ", 사유: "
                + requestReason;
    }

    private String buildTransferAcceptReason(
            Department targetDepartment,
            String responseReason
    ) {
        return "이관 승인: 담당 부서가 "
                + targetDepartment.getName()
                + "(으)로 변경되었습니다. 응답 사유: "
                + responseReason;
    }

    private String buildTransferRejectReason(String responseReason) {
        return "이관 거절: " + responseReason;
    }
}