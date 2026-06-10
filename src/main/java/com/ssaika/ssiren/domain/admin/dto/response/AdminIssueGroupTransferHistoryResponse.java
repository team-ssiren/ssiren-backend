package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.domain.report.entity.IssueGroupTransferHistory;
import com.ssaika.ssiren.domain.report.entity.IssueGroupTransferHistoryStatus;

import java.time.LocalDateTime;

public record AdminIssueGroupTransferHistoryResponse(
        Long transferHistoryId,
        Long issueGroupId,
        String issueGroupTitle,
        AdminTransferDepartmentResponse fromDepartment,
        AdminTransferDepartmentResponse targetDepartment,
        Long requestUserId,
        Long responseUserId,
        IssueGroupTransferHistoryStatus status,
        String requestReason,
        LocalDateTime requestedAt,
        String responseReason,
        LocalDateTime responseAt,
        Integer transferredReportCount
) {

    public static AdminIssueGroupTransferHistoryResponse of(
            IssueGroupTransferHistory history,
            Integer transferredReportCount
    ) {
        Long responseUserId = history.getResponseUser() == null
                ? null
                : history.getResponseUser().getId();

        return new AdminIssueGroupTransferHistoryResponse(
                history.getId(),
                history.getIssueGroup().getId(),
                history.getIssueGroup().getTitle(),
                AdminTransferDepartmentResponse.from(history.getFromDepartment()),
                AdminTransferDepartmentResponse.from(history.getTargetDepartment()),
                history.getRequestUser().getId(),
                responseUserId,
                history.getStatus(),
                history.getRequestReason(),
                history.getRequestedAt(),
                history.getResponseReason(),
                history.getResponseAt(),
                transferredReportCount
        );
    }
}