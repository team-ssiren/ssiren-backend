package com.ssaika.ssiren.domain.admin.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.dto.response.ReportImageResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportResponse;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportImage;
import java.util.List;

public record AdminReportResponse(
        ReportResponse report,
        List<ReportImageResponse> reportImages
) {

    public static AdminReportResponse from(
            Report report,
            List<ReportImage> reportImages,
            ObjectMapper objectMapper) {
        return new AdminReportResponse(
                ReportResponse.from(report, objectMapper),
                reportImages.stream()
                        .map(ReportImageResponse::from)
                        .toList()
        );
    }
}