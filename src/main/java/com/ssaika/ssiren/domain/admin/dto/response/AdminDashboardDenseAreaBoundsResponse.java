package com.ssaika.ssiren.domain.admin.dto.response;

import java.math.BigDecimal;

public record AdminDashboardDenseAreaBoundsResponse(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng
) {
}