package com.ssaika.ssiren.domain.report.address;

import com.ssaika.ssiren.domain.report.address.KakaoLocalClient.AddressDocument;
import com.ssaika.ssiren.domain.report.address.KakaoLocalClient.KakaoCoord2AddressResponse;
import com.ssaika.ssiren.domain.report.address.KakaoLocalClient.KakaoCoord2RegionCodeResponse;
import com.ssaika.ssiren.domain.report.address.KakaoLocalClient.RegionDocument;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class KakaoAddressResolver implements AddressResolver {

    private static final String ADDRESS_NOT_RESOLVED = "주소 확인 필요";
    private static final String ADMINISTRATIVE_REGION_TYPE = "H";
    private static final String LEGAL_REGION_TYPE = "B";

    private final KakaoLocalClient kakaoLocalClient;

    public KakaoAddressResolver(KakaoLocalClient kakaoLocalClient) {
        this.kakaoLocalClient = kakaoLocalClient;
    }

    @Override
    public AddressSnapshot resolve(BigDecimal latitude, BigDecimal longitude) {
        AddressValues addressValues = kakaoLocalClient.coord2Address(latitude, longitude)
            .map(this::resolveAddressValues)
            .orElseGet(AddressValues::empty);
        RegionValues regionValues = kakaoLocalClient.coord2RegionCode(latitude, longitude)
            .map(this::resolveRegionValues)
            .orElseGet(RegionValues::empty);

        String roadAddress = firstNotBlank(
            addressValues.roadAddress(),
            addressValues.jibunAddress(),
            fallbackRoadAddress(latitude, longitude)
        );

        return new AddressSnapshot(
            roadAddress,
            firstNotBlank(addressValues.jibunAddress(), ADDRESS_NOT_RESOLVED),
            firstNotBlank(regionValues.sido(), addressValues.sido(), ADDRESS_NOT_RESOLVED),
            firstNotBlank(regionValues.sigungu(), addressValues.sigungu(), ADDRESS_NOT_RESOLVED),
            firstNotBlank(regionValues.eupmyeondong(), addressValues.eupmyeondong(), ADDRESS_NOT_RESOLVED)
        );
    }

    private AddressValues resolveAddressValues(KakaoCoord2AddressResponse response) {
        List<AddressDocument> documents = response.documents();
        if (documents == null || documents.isEmpty()) {
            return AddressValues.empty();
        }

        AddressDocument document = documents.get(0);
        String roadAddress = document.roadAddress() == null ? null : document.roadAddress().addressName();
        String jibunAddress = document.address() == null ? null : document.address().addressName();

        return new AddressValues(
            roadAddress,
            jibunAddress,
            firstNotBlank(
                document.roadAddress() == null ? null : document.roadAddress().sido(),
                document.address() == null ? null : document.address().sido()
            ),
            firstNotBlank(
                document.roadAddress() == null ? null : document.roadAddress().sigungu(),
                document.address() == null ? null : document.address().sigungu()
            ),
            firstNotBlank(
                document.roadAddress() == null ? null : document.roadAddress().eupmyeondong(),
                document.address() == null ? null : document.address().eupmyeondong()
            )
        );
    }

    private RegionValues resolveRegionValues(KakaoCoord2RegionCodeResponse response) {
        List<RegionDocument> documents = response.documents();
        if (documents == null || documents.isEmpty()) {
            return RegionValues.empty();
        }

        return documents.stream()
            .filter(document -> ADMINISTRATIVE_REGION_TYPE.equals(document.regionType()))
            .findFirst()
            .or(() -> documents.stream()
                .filter(document -> LEGAL_REGION_TYPE.equals(document.regionType()))
                .findFirst())
            .map(document -> new RegionValues(
                document.sido(),
                document.sigungu(),
                document.eupmyeondong()
            ))
            .orElseGet(RegionValues::empty);
    }

    private String fallbackRoadAddress(BigDecimal latitude, BigDecimal longitude) {
        String coordinateText = latitude.setScale(7, RoundingMode.HALF_UP)
            + ", "
            + longitude.setScale(7, RoundingMode.HALF_UP);
        return ADDRESS_NOT_RESOLVED + " (" + coordinateText + ")";
    }

    private String firstNotBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private record AddressValues(
        String roadAddress,
        String jibunAddress,
        String sido,
        String sigungu,
        String eupmyeondong
    ) {

        private static AddressValues empty() {
            return new AddressValues(null, null, null, null, null);
        }
    }

    private record RegionValues(
        String sido,
        String sigungu,
        String eupmyeondong
    ) {

        private static RegionValues empty() {
            return new RegionValues(null, null, null);
        }
    }
}
