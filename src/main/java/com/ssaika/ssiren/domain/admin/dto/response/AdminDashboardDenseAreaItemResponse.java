package com.ssaika.ssiren.domain.admin.dto.response;

import java.math.BigDecimal;

public record AdminDashboardDenseAreaItemResponse(
        Long issueGroupCount,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        AdminDashboardDenseAreaBoundsResponse bounds
) {
}