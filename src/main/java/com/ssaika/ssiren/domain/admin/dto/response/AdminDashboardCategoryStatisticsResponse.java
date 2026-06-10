package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.domain.admin.dto.projection.AdminDashboardCategoryCountProjection;

import java.util.List;

public record AdminDashboardCategoryStatisticsResponse(
        List<AdminDashboardCategoryCountResponse> categories
) {

    public static AdminDashboardCategoryStatisticsResponse from(
            List<AdminDashboardCategoryCountProjection> projections
    ) {
        return new AdminDashboardCategoryStatisticsResponse(
                projections.stream()
                        .map(AdminDashboardCategoryCountResponse::from)
                        .toList()
        );
    }
}