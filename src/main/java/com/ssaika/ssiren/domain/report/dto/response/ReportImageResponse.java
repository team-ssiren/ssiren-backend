package com.ssaika.ssiren.domain.report.dto.response;

import com.ssaika.ssiren.domain.report.entity.ReportImage;

public record ReportImageResponse(
    Long id,
    String imageUrl,
    Integer sortOrder,
    Long reportId
) {

    public static ReportImageResponse from(ReportImage reportImage) {
        return new ReportImageResponse(
            reportImage.getId(),
            reportImage.getImageUrl(),
            reportImage.getSortOrder(),
            reportImage.getReport().getId()
        );
    }
}
