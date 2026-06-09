package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.global.enums.IssueGroupStatus;
import com.ssaika.ssiren.global.enums.ReportStatus;

import java.util.List;

public record AdminIssueGroupStatusUpdateResponse(
        Long issueGroupId,
        IssueGroupStatus issueGroupStatus,
        ReportStatus reportStatus,
        Integer changedReportCount,
        List<Long> changedReportIds
) {

    public static AdminIssueGroupStatusUpdateResponse of(
            Long issueGroupId,
            IssueGroupStatus issueGroupStatus,
            ReportStatus reportStatus,
            List<Long> changedReportIds
    ) {
        return new AdminIssueGroupStatusUpdateResponse(
                issueGroupId,
                issueGroupStatus,
                reportStatus,
                changedReportIds.size(),
                changedReportIds
        );
    }
}