package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.domain.report.entity.ReportCategory;

public record AdminParentCategoryResponse(
        Long id,
        String categoryCode,
        String categoryName
) {

    public static AdminParentCategoryResponse from(ReportCategory category) {
        return new AdminParentCategoryResponse(
                category.getId(),
                category.getCategoryCode(),
                category.getCategoryName()
        );
    }
}