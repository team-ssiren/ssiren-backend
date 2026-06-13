package com.ssaika.ssiren.domain.report.address;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssaika.ssiren.global.config.KakaoLocalProperties;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class KakaoLocalClient {

    private static final String AUTHORIZATION_PREFIX = "KakaoAK ";
    private static final String COORDINATE_SYSTEM = "WGS84";

    private final WebClient kakaoLocalWebClient;
    private final KakaoLocalProperties kakaoLocalProperties;

    public KakaoLocalClient(
        @Qualifier("kakaoLocalWebClient") WebClient kakaoLocalWebClient,
        KakaoLocalProperties kakaoLocalProperties
    ) {
        this.kakaoLocalWebClient = kakaoLocalWebClient;
        this.kakaoLocalProperties = kakaoLocalProperties;
    }

    public Optional<KakaoCoord2AddressResponse> coord2Address(BigDecimal latitude, BigDecimal longitude) {
        if (!kakaoLocalProperties.hasRestApiKey()) {
            return Optional.empty();
        }

        try {
            KakaoCoord2AddressResponse response = kakaoLocalWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/local/geo/coord2address.json")
                    .queryParam("x", longitude)
                    .queryParam("y", latitude)
                    .queryParam("input_coord", COORDINATE_SYSTEM)
                    .build())
                .header("Authorization", AUTHORIZATION_PREFIX + kakaoLocalProperties.getRestApiKey())
                .retrieve()
                .bodyToMono(KakaoCoord2AddressResponse.class)
                .block();

            return Optional.ofNullable(response);
        } catch (RuntimeException e) {
            logKakaoFailure("coord2address", latitude, longitude, e);
            return Optional.empty();
        }
    }

    public Optional<KakaoCoord2RegionCodeResponse> coord2RegionCode(BigDecimal latitude, BigDecimal longitude) {
        if (!kakaoLocalProperties.hasRestApiKey()) {
            return Optional.empty();
        }

        try {
            KakaoCoord2RegionCodeResponse response = kakaoLocalWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/local/geo/coord2regioncode.json")
                    .queryParam("x", longitude)
                    .queryParam("y", latitude)
                    .queryParam("input_coord", COORDINATE_SYSTEM)
                    .build())
                .header("Authorization", AUTHORIZATION_PREFIX + kakaoLocalProperties.getRestApiKey())
                .retrieve()
                .bodyToMono(KakaoCoord2RegionCodeResponse.class)
                .block();

            return Optional.ofNullable(response);
        } catch (RuntimeException e) {
            logKakaoFailure("coord2regioncode", latitude, longitude, e);
            return Optional.empty();
        }
    }

    public Optional<KakaoKeywordSearchResponse> searchKeyword(
        String query,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        int size
    ) {
        if (!kakaoLocalProperties.hasRestApiKey() || query == null || query.isBlank()) {
            return Optional.empty();
        }

        try {
            KakaoKeywordSearchResponse response = kakaoLocalWebClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder
                        .path("/v2/local/search/keyword.json")
                        .queryParam("query", query.trim())
                        .queryParam("size", size);
                    if (latitude != null && longitude != null) {
                        builder
                            .queryParam("x", longitude)
                            .queryParam("y", latitude)
                            .queryParam("radius", radiusMeters == null ? 5000 : radiusMeters);
                    }
                    return builder.build();
                })
                .header("Authorization", AUTHORIZATION_PREFIX + kakaoLocalProperties.getRestApiKey())
                .retrieve()
                .bodyToMono(KakaoKeywordSearchResponse.class)
                .block();

            return Optional.ofNullable(response);
        } catch (RuntimeException e) {
            logKakaoSearchFailure("keyword", query, e);
            return Optional.empty();
        }
    }

    public Optional<KakaoAddressSearchResponse> searchAddress(String query, int size) {
        if (!kakaoLocalProperties.hasRestApiKey() || query == null || query.isBlank()) {
            return Optional.empty();
        }

        try {
            KakaoAddressSearchResponse response = kakaoLocalWebClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/v2/local/search/address.json")
                    .queryParam("query", query.trim())
                    .queryParam("size", size)
                    .build())
                .header("Authorization", AUTHORIZATION_PREFIX + kakaoLocalProperties.getRestApiKey())
                .retrieve()
                .bodyToMono(KakaoAddressSearchResponse.class)
                .block();

            return Optional.ofNullable(response);
        } catch (RuntimeException e) {
            logKakaoSearchFailure("address", query, e);
            return Optional.empty();
        }
    }

    private void logKakaoFailure(String apiName, BigDecimal latitude, BigDecimal longitude, RuntimeException e) {
        if (e instanceof WebClientResponseException responseException) {
            log.warn(
                "Failed to call Kakao {} API. status={}, latitude={}, longitude={}, body={}",
                apiName,
                responseException.getStatusCode(),
                latitude,
                longitude,
                responseException.getResponseBodyAsString()
            );
            return;
        }

        log.warn(
            "Failed to call Kakao {} API. latitude={}, longitude={}, message={}",
            apiName,
            latitude,
            longitude,
            e.getMessage()
        );
    }

    private void logKakaoSearchFailure(String apiName, String query, RuntimeException e) {
        if (e instanceof WebClientResponseException responseException) {
            log.warn(
                "Failed to call Kakao {} search API. status={}, query={}, body={}",
                apiName,
                responseException.getStatusCode(),
                query,
                responseException.getResponseBodyAsString()
            );
            return;
        }

        log.warn("Failed to call Kakao {} search API. query={}, message={}", apiName, query, e.getMessage());
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoCoord2AddressResponse(List<AddressDocument> documents) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressDocument(
        Address address,
        @JsonProperty("road_address") RoadAddress roadAddress
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Address(
        @JsonProperty("address_name") String addressName,
        @JsonProperty("region_1depth_name") String sido,
        @JsonProperty("region_2depth_name") String sigungu,
        @JsonProperty("region_3depth_name") String eupmyeondong
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RoadAddress(
        @JsonProperty("address_name") String addressName,
        @JsonProperty("region_1depth_name") String sido,
        @JsonProperty("region_2depth_name") String sigungu,
        @JsonProperty("region_3depth_name") String eupmyeondong
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoCoord2RegionCodeResponse(List<RegionDocument> documents) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoKeywordSearchResponse(List<KeywordDocument> documents) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KeywordDocument(
        String id,
        @JsonProperty("place_name") String placeName,
        @JsonProperty("category_name") String categoryName,
        @JsonProperty("address_name") String addressName,
        @JsonProperty("road_address_name") String roadAddressName,
        String phone,
        String x,
        String y,
        @JsonProperty("place_url") String placeUrl,
        String distance
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record KakaoAddressSearchResponse(List<AddressSearchDocument> documents) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AddressSearchDocument(
        @JsonProperty("address_name") String addressName,
        @JsonProperty("address_type") String addressType,
        String x,
        String y,
        Address address,
        @JsonProperty("road_address") RoadAddress roadAddress
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RegionDocument(
        @JsonProperty("region_type") String regionType,
        @JsonProperty("region_1depth_name") String sido,
        @JsonProperty("region_2depth_name") String sigungu,
        @JsonProperty("region_3depth_name") String eupmyeondong
    ) {
    }
}
