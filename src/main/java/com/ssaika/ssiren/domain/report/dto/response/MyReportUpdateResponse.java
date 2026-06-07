package com.ssaika.ssiren.domain.report.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.Report;

public record MyReportUpdateResponse(
    ReportResponse report,
    ReportCategoryResponse category,
    ReportIssueGroupResponse issueGroup
) {

    public static MyReportUpdateResponse from(Report report, ObjectMapper objectMapper) {
        return new MyReportUpdateResponse(
            ReportResponse.from(report, objectMapper),
            ReportCategoryResponse.from(report.getCategory()),
            ReportIssueGroupResponse.from(report.getIssueGroup())
        );
    }
}
