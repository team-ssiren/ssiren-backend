package com.ssaika.ssiren.domain.report.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportImage;
import java.util.List;

public record MyReportDeleteResponse(
    Boolean deleted,
    ReportResponse deletedReport,
    List<ReportImageResponse> deletedReportImages,
    ReportIssueGroupResponse updatedIssueGroup
) {

    public static MyReportDeleteResponse from(
        Report report,
        List<ReportImage> reportImages,
        IssueGroup issueGroup,
        ObjectMapper objectMapper) {
        return new MyReportDeleteResponse(
            true,
            ReportResponse.from(report, objectMapper),
            reportImages.stream()
                .map(ReportImageResponse::from)
                .toList(),
            issueGroup == null ? null : ReportIssueGroupResponse.from(issueGroup)
        );
    }
}
