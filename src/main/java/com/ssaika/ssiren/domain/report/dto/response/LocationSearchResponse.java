package com.ssaika.ssiren.domain.report.dto.response;

import java.util.List;

public record LocationSearchResponse(
    List<LocationSearchPlaceResponse> places
) {
    public static LocationSearchResponse from(List<LocationSearchPlaceResponse> places) {
        return new LocationSearchResponse(places);
    }
}
