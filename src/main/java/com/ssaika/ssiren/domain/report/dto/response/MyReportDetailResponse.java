package com.ssaika.ssiren.domain.report.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportImage;
import com.ssaika.ssiren.domain.report.entity.ReportReactionLog;
import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import java.util.List;

public record MyReportDetailResponse(
    ReportResponse report,
    List<ReportImageResponse> reportImages,
    ReportCategoryResponse category,
    ReportCategoryResponse parentCategory,
    ReportIssueGroupResponse issueGroup,
    ReportDepartmentResponse department,
    ReportAgencyTypeResponse agencyType,
    List<ReportStatusHistoryResponse> statusHistories,
    List<ReportReactionLogResponse> reactionLogs
) {

    public static MyReportDetailResponse from(
        Report report,
        List<ReportImage> reportImages,
        List<ReportStatusHistory> statusHistories,
        List<ReportReactionLog> reactionLogs,
        ObjectMapper objectMapper) {
        return new MyReportDetailResponse(
            ReportResponse.from(report, objectMapper),
            reportImages.stream()
                .map(ReportImageResponse::from)
                .toList(),
            ReportCategoryResponse.from(report.getCategory()),
            report.getCategory().getParentCategory() == null
                ? null
                : ReportCategoryResponse.from(report.getCategory().getParentCategory()),
            ReportIssueGroupResponse.from(report.getIssueGroup()),
            ReportDepartmentResponse.from(report.getDepartment()),
            ReportAgencyTypeResponse.from(report.getDepartment().getAgencyType()),
            statusHistories.stream()
                .map(ReportStatusHistoryResponse::from)
                .toList(),
            reactionLogs.stream()
                .map(ReportReactionLogResponse::from)
                .toList()
        );
    }
}
