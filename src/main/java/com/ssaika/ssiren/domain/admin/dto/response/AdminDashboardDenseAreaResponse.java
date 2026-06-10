package com.ssaika.ssiren.domain.admin.dto.response;

import java.util.List;

public record AdminDashboardDenseAreaResponse(
        List<AdminDashboardDenseAreaItemResponse> denseAreas
) {

    public static AdminDashboardDenseAreaResponse from(
            List<AdminDashboardDenseAreaItemResponse> denseAreas
    ) {
        return new AdminDashboardDenseAreaResponse(denseAreas);
    }
}