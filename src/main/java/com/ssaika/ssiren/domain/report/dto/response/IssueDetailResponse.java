package com.ssaika.ssiren.domain.report.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.domain.report.entity.Report;
import java.util.List;

public record IssueDetailResponse(
    ReportIssueGroupResponse issueGroup,
    ReportResponse representativeReport,
    List<ReportResponse> reports,
    ReportCategoryResponse category
) {

    public static IssueDetailResponse from(
        IssueGroup issueGroup,
        Report representativeReport,
        List<Report> reports,
        ObjectMapper objectMapper) {
        return new IssueDetailResponse(
            ReportIssueGroupResponse.from(issueGroup),
            representativeReport == null ? null : ReportResponse.from(representativeReport, objectMapper),
            reports.stream()
                .map(report -> ReportResponse.from(report, objectMapper))
                .toList(),
            representativeReport == null ? null : ReportCategoryResponse.from(representativeReport.getCategory())
        );
    }
}
