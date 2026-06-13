package com.ssaika.ssiren.domain.report.controller;

import com.ssaika.ssiren.domain.report.address.LocationSearchService;
import com.ssaika.ssiren.domain.report.dto.response.LocationSearchResponse;
import com.ssaika.ssiren.global.dto.BaseResponse;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/maps")
@Validated
public class LocationController {

    private final LocationSearchService locationSearchService;

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<LocationSearchResponse>> searchLocations(
        @RequestParam String query,
        @RequestParam(required = false) BigDecimal latitude,
        @RequestParam(required = false) BigDecimal longitude,
        @RequestParam(required = false) Integer radiusMeters
    ) {
        LocationSearchResponse response = LocationSearchResponse.from(
            locationSearchService.search(query, latitude, longitude, radiusMeters)
        );

        return ResponseEntity.ok(BaseResponse.success("주소 검색 성공", response));
    }
}
