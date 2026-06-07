package com.ssaika.ssiren.domain.report.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportImage;
import java.util.List;

public record MyReportResponse(
    ReportResponse report,
    List<ReportImageResponse> reportImages,
    ReportCategoryResponse category,
    ReportIssueGroupResponse issueGroup,
    ReportDepartmentResponse department
) {

    public static MyReportResponse from(
        Report report,
        List<ReportImage> reportImages,
        ObjectMapper objectMapper) {
        return new MyReportResponse(
            ReportResponse.from(report, objectMapper),
            reportImages.stream()
                .map(ReportImageResponse::from)
                .toList(),
            ReportCategoryResponse.from(report.getCategory()),
            ReportIssueGroupResponse.from(report.getIssueGroup()),
            ReportDepartmentResponse.from(report.getDepartment())
        );
    }
}
