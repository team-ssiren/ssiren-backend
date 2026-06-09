package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.domain.report.entity.ReportCategory;

public record AdminReportCategoryResponse(
        Long id,
        String categoryCode,
        String categoryName,
        AdminParentCategoryResponse parentCategory
) {

    public static AdminReportCategoryResponse from(ReportCategory category) {
        ReportCategory parentCategory = category.getParentCategory();

        return new AdminReportCategoryResponse(
                category.getId(),
                category.getCategoryCode(),
                category.getCategoryName(),
                parentCategory == null ? null : AdminParentCategoryResponse.from(parentCategory)
        );
    }
}