package com.ssaika.ssiren.domain.report.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportImage;
import java.util.List;

public record ReportCreateResponse(
    ReportResponse report,
    List<ReportImageResponse> reportImages,
    ReportCategoryResponse category,
    ReportCategoryResponse parentCategory,
    ReportIssueGroupResponse issueGroup,
    ReportDepartmentResponse department,
    ReportAgencyTypeResponse agencyType
) {

    public static ReportCreateResponse from(
        Report report,
        List<ReportImage> reportImages,
        IssueGroup issueGroup,
        ObjectMapper objectMapper) {
        return new ReportCreateResponse(
            ReportResponse.from(report, objectMapper),
            reportImages.stream()
                .map(ReportImageResponse::from)
                .toList(),
            ReportCategoryResponse.from(report.getCategory()),
            report.getCategory().getParentCategory() == null
                ? null
                : ReportCategoryResponse.from(report.getCategory().getParentCategory()),
            ReportIssueGroupResponse.from(issueGroup),
            ReportDepartmentResponse.from(report.getDepartment()),
            ReportAgencyTypeResponse.from(report.getDepartment().getAgencyType())
        );
    }
}
