package com.ssaika.ssiren.domain.admin.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.dto.response.ReportCategoryResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportDepartmentResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportIssueGroupResponse;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportImage;
import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import java.util.List;

public record AdminIssueResponse(
        ReportIssueGroupResponse issueGroup,
        AdminRepresentativeReportResponse representativeReport,
        ReportCategoryResponse category,
        ReportDepartmentResponse department
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
                ReportCategoryResponse.from(representativeReport.getCategory()),
                ReportDepartmentResponse.from(representativeReport.getDepartment())
        );
    }
}