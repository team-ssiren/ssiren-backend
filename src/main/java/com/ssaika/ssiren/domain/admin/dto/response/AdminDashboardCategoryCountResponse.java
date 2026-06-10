package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.domain.admin.dto.projection.AdminDashboardCategoryCountProjection;

public record AdminDashboardCategoryCountResponse(
        Long categoryId,
        String categoryCode,
        String categoryName,
        Long reportCount
) {

    public static AdminDashboardCategoryCountResponse from(
            AdminDashboardCategoryCountProjection projection
    ) {
        return new AdminDashboardCategoryCountResponse(
                projection.getCategoryId(),
                projection.getCategoryCode(),
                projection.getCategoryName(),
                projection.getReportCount()
        );
    }
}