package com.ssaika.ssiren.domain.report.dto.response;

import com.ssaika.ssiren.domain.report.entity.ReportCategory;
import java.time.LocalDateTime;

public record ReportCategoryResponse(
    Long id,
    String categoryCode,
    String categoryName,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long parentCategoryId
) {

    public static ReportCategoryResponse from(ReportCategory category) {
        return new ReportCategoryResponse(
            category.getId(),
            category.getCategoryCode(),
            category.getCategoryName(),
            category.getCreatedAt(),
            category.getUpdatedAt(),
            category.getParentCategory() == null ? null : category.getParentCategory().getId()
        );
    }
}
