package com.ssaika.ssiren.domain.report.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportReactionLog;

public record ReportReactionResponse(
    ReportReactionLogResponse reactionLog,
    ReportResponse report,
    ReportIssueGroupResponse issueGroup
) {

    public static ReportReactionResponse from(
        ReportReactionLog reactionLog,
        Report report,
        ObjectMapper objectMapper) {
        return new ReportReactionResponse(
            ReportReactionLogResponse.from(reactionLog),
            ReportResponse.from(report, objectMapper),
            ReportIssueGroupResponse.from(report.getIssueGroup())
        );
    }
}
