package com.ssaika.ssiren.domain.report.address;

import com.ssaika.ssiren.domain.report.address.KakaoLocalClient.AddressSearchDocument;
import com.ssaika.ssiren.domain.report.address.KakaoLocalClient.KeywordDocument;
import com.ssaika.ssiren.domain.report.dto.response.LocationSearchPlaceResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class LocationSearchService {

    private static final int KAKAO_SEARCH_SIZE = 15;
    private static final int RESULT_LIMIT = 10;

    private final KakaoLocalClient kakaoLocalClient;

    public LocationSearchService(KakaoLocalClient kakaoLocalClient) {
        this.kakaoLocalClient = kakaoLocalClient;
    }

    public List<LocationSearchPlaceResponse> search(
        String query,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters
    ) {
        String trimmed = query == null ? "" : query.trim();
        if (trimmed.length() < 2) {
            return List.of();
        }

        List<LocationSearchPlaceResponse> results = new ArrayList<>();
        searchKeyword(trimmed, null, null, null, results);
        stationQuery(trimmed).forEach(stationKeyword -> searchKeyword(stationKeyword, null, null, null, results));

        kakaoLocalClient.searchAddress(trimmed, KAKAO_SEARCH_SIZE)
            .ifPresent(response -> response.documents().stream()
                .map(this::fromAddress)
                .forEach(results::add));

        return deduplicate(results).stream()
            .sorted(searchComparator(trimmed))
            .limit(RESULT_LIMIT)
            .toList();
    }

    private void searchKeyword(
        String query,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        List<LocationSearchPlaceResponse> results
    ) {
        kakaoLocalClient.searchKeyword(query, latitude, longitude, radiusMeters, KAKAO_SEARCH_SIZE)
            .ifPresent(response -> response.documents().stream()
                .map(this::fromKeyword)
                .forEach(results::add));
    }

    private List<String> stationQuery(String query) {
        if (query.endsWith("역") || query.length() > 4 || !query.matches(".*[가-힣].*")) {
            return List.of();
        }
        return List.of(query + "역");
    }

    private LocationSearchPlaceResponse fromKeyword(KeywordDocument document) {
        return new LocationSearchPlaceResponse(
            document.id(),
            document.placeName(),
            document.categoryName(),
            document.addressName(),
            document.roadAddressName(),
            document.phone(),
            toDecimal(document.y()),
            toDecimal(document.x()),
            document.placeUrl(),
            document.distance()
        );
    }

    private LocationSearchPlaceResponse fromAddress(AddressSearchDocument document) {
        String roadAddressName = document.roadAddress() == null ? null : document.roadAddress().addressName();
        String addressName = document.address() == null ? document.addressName() : document.address().addressName();
        String placeName = firstNotBlank(roadAddressName, addressName, document.addressName());

        return new LocationSearchPlaceResponse(
            "address-" + document.y() + "-" + document.x(),
            placeName,
            document.addressType(),
            addressName,
            roadAddressName,
            null,
            toDecimal(document.y()),
            toDecimal(document.x()),
            null,
            null
        );
    }

    private List<LocationSearchPlaceResponse> deduplicate(List<LocationSearchPlaceResponse> results) {
        Map<String, LocationSearchPlaceResponse> unique = new LinkedHashMap<>();
        for (LocationSearchPlaceResponse result : results) {
            if (result.latitude() == null || result.longitude() == null) {
                continue;
            }
            unique.putIfAbsent(dedupeKey(result), result);
        }
        return new ArrayList<>(unique.values());
    }

    private Comparator<LocationSearchPlaceResponse> searchComparator(String query) {
        return Comparator
            .comparingInt((LocationSearchPlaceResponse result) -> searchScore(result, query))
            .reversed()
            .thenComparingInt(this::distanceValue);
    }

    private int searchScore(LocationSearchPlaceResponse result, String query) {
        String normalizedQuery = normalize(query);
        String placeName = normalize(result.placeName());
        String addressName = normalize(result.addressName());
        String roadAddressName = normalize(result.roadAddressName());
        String categoryName = normalize(result.categoryName());
        boolean stationSearch = normalizedQuery.endsWith("역");
        boolean stationCategory = categoryName.contains("지하철역") || categoryName.contains("전철역");
        int score = 0;

        if (placeName.equals(normalizedQuery)) {
            score += 1000;
        } else if (placeName.startsWith(normalizedQuery)) {
            score += 800;
        } else if (placeName.contains(normalizedQuery)) {
            score += 650;
        }

        if (roadAddressName.contains(normalizedQuery) || addressName.contains(normalizedQuery)) {
            score += 520;
        }

        if (stationCategory) {
            score += stationSearch ? 500 : 220;
        }

        if (stationSearch && !placeName.contains(normalizedQuery)) {
            score -= 650;
        }

        if (!placeName.contains(normalizedQuery)
            && !addressName.contains(normalizedQuery)
            && !roadAddressName.contains(normalizedQuery)) {
            score -= 250;
        }

        return score;
    }

    private int distanceValue(LocationSearchPlaceResponse result) {
        if (result.distance() == null || result.distance().isBlank()) {
            return Integer.MAX_VALUE;
        }
        try {
            return Integer.parseInt(result.distance());
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.replaceAll("\\s+", "").toLowerCase();
    }

    private String dedupeKey(LocationSearchPlaceResponse result) {
        return result.placeName() + ":" + result.latitude().stripTrailingZeros() + ":" + result.longitude().stripTrailingZeros();
    }

    private BigDecimal toDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return new BigDecimal(value);
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return "주소 검색 결과";
    }
}
