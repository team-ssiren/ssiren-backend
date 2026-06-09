package com.ssaika.ssiren.domain.admin.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.dto.response.ReportIssueGroupResponse;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportImage;
import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import java.util.List;

public record AdminIssueResponse(
        ReportIssueGroupResponse issueGroup,
        AdminRepresentativeReportResponse representativeReport,
        AdminReportCategoryResponse category,
        AdminReportDepartmentResponse department
) {

    public static AdminIssueResponse from(
            Report representativeReport,
            List<ReportImage> reportImages,
            List<ReportStatusHistory> statusHistories,
            ObjectMapper objectMapper) {
        return new AdminIssueResponse(
                ReportIssueGroupResponse.from(representativeReport.getIssueGroup()),
                AdminRepresentativeReportResponse.from(
                        representativeReport,
                        reportImages,
                        statusHistories,
                        objectMapper
                ),
                AdminReportCategoryResponse.from(representativeReport.getCategory()),
                AdminReportDepartmentResponse.from(representativeReport.getDepartment())
        );
    }
}