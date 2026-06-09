package com.ssaika.ssiren.domain.admin.dto.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.dto.response.ReportResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportImageResponse;
import com.ssaika.ssiren.domain.report.dto.response.ReportStatusHistoryResponse;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportImage;
import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import java.util.List;

public record AdminRepresentativeReportResponse(
        ReportResponse report,
        List<ReportImageResponse> reportImages,
        List<ReportStatusHistoryResponse> statusHistories
) {

    public static AdminRepresentativeReportResponse from(
            Report report,
            List<ReportImage> reportImages,
            List<ReportStatusHistory> statusHistories,
            ObjectMapper objectMapper) {
        return new AdminRepresentativeReportResponse(
                ReportResponse.from(report, objectMapper),
                reportImages.stream()
                        .map(ReportImageResponse::from)
                        .toList(),
                statusHistories.stream()
                        .map(ReportStatusHistoryResponse::from)
                        .toList()
        );
    }
}