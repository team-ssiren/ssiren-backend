package com.ssaika.ssiren.domain.report.dto.response;

import java.math.BigDecimal;

public record LocationSearchPlaceResponse(
    String id,
    String placeName,
    String categoryName,
    String addressName,
    String roadAddressName,
    String phone,
    BigDecimal latitude,
    BigDecimal longitude,
    String placeUrl,
    String distance
) {
}
