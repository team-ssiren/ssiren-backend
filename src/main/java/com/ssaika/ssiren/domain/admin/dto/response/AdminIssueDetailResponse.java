package com.ssaika.ssiren.domain.admin.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.dto.response.ReportIssueGroupResponse;
import com.ssaika.ssiren.domain.report.entity.*;

import java.util.List;
import java.util.Map;

public record AdminIssueDetailResponse(
        ReportIssueGroupResponse issueGroup,
        AdminRepresentativeReportResponse representativeReport,
        List<AdminReportResponse> reports,
        AdminReportCategoryResponse category,
        AdminReportDepartmentResponse department
) {

    public static AdminIssueDetailResponse from(
            IssueGroup issueGroup,
            Report representativeReport,
            List<Report> reports,
            Map<Long, List<ReportImage>> reportImageMap,
            List<ReportStatusHistory> representativeStatusHistories,
            ObjectMapper objectMapper) {
        ReportCategory category = representativeReport.getCategory();
        ReportCategory parentCategory = category.getParentCategory();

        return new AdminIssueDetailResponse(
                ReportIssueGroupResponse.from(issueGroup),
                AdminRepresentativeReportResponse.from(
                        representativeReport,
                        reportImageMap.getOrDefault(representativeReport.getId(), List.of()),
                        representativeStatusHistories,
                        objectMapper
                ),
                reports.stream()
                        .map(report -> AdminReportResponse.from(
                                report,
                                reportImageMap.getOrDefault(report.getId(), List.of()),
                                objectMapper
                        ))
                        .toList(),
                AdminReportCategoryResponse.from(representativeReport.getCategory()),
                AdminReportDepartmentResponse.from(representativeReport.getDepartment())
        );
    }
}