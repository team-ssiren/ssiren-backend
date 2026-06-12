package com.ssaika.ssiren.domain.report.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.Report;

public record IssueResponse(
    ReportIssueGroupResponse issueGroup,
    ReportResponse representativeReport,
    ReportCategoryResponse category,
    ReportDepartmentResponse department,
    ReportAgencyTypeResponse agencyType
) {

    public static IssueResponse from(Report representativeReport, ObjectMapper objectMapper) {
        return new IssueResponse(
            ReportIssueGroupResponse.from(representativeReport.getIssueGroup()),
            ReportResponse.from(representativeReport, objectMapper),
            ReportCategoryResponse.from(representativeReport.getCategory()),
            ReportDepartmentResponse.from(representativeReport.getDepartment()),
            ReportAgencyTypeResponse.from(representativeReport.getDepartment().getAgencyType())
        );
    }
}
