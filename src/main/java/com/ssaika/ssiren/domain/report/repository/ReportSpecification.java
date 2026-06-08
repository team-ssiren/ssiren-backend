package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.global.enums.IssueGroupStatus;
import com.ssaika.ssiren.global.enums.ReportStatus;
import com.ssaika.ssiren.global.enums.ReportVisibility;
import jakarta.persistence.criteria.Expression;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public class ReportSpecification {

    private ReportSpecification() {
    }

    public static Specification<Report> belongsToUser(Long userId) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Report> hasStatus(ReportStatus status) {
        return (root, query, criteriaBuilder) -> status == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Report> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> categoryId == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Report> hasAgencyType(Long agencyId) {
        return (root, query, criteriaBuilder) -> agencyId == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.equal(root.get("department").get("agencyType").get("id"), agencyId);
    }

    public static Specification<Report> hasVisibility(ReportVisibility visibility) {
        return (root, query, criteriaBuilder) -> visibility == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.equal(root.get("visibility"), visibility);
    }

    public static Specification<Report> isNotDeleted() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.isFalse(root.get("isDeleted"));
    }

    public static Specification<Report> isRepresentative() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.isTrue(root.get("isRepresentative"));
    }

    public static Specification<Report> hasSido(String sido) {
        return (root, query, criteriaBuilder) -> sido == null || sido.isBlank()
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.equal(root.get("sido"), sido);
    }

    public static Specification<Report> hasSigungu(String sigungu) {
        return (root, query, criteriaBuilder) -> sigungu == null || sigungu.isBlank()
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.equal(root.get("sigungu"), sigungu);
    }

    public static Specification<Report> hasEupmyeondong(String eupmyeondong) {
        return (root, query, criteriaBuilder) -> eupmyeondong == null || eupmyeondong.isBlank()
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.equal(root.get("eupmyeondong"), eupmyeondong);
    }

    public static Specification<Report> createdAtFrom(LocalDateTime from) {
        return (root, query, criteriaBuilder) -> from == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Report> createdAtTo(LocalDateTime to) {
        return (root, query, criteriaBuilder) -> to == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    public static Specification<Report> hasIssueGroupStatus(IssueGroupStatus status) {
        return (root, query, criteriaBuilder) -> status == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.equal(root.get("issueGroup").get("status"), status);
    }

    public static Specification<Report> issueGroupRiskScoreFrom(BigDecimal riskMin) {
        return (root, query, criteriaBuilder) -> riskMin == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.greaterThanOrEqualTo(root.get("issueGroup").get("riskScore"), riskMin);
    }

    public static Specification<Report> issueGroupRiskScoreTo(BigDecimal riskMax) {
        return (root, query, criteriaBuilder) -> riskMax == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.lessThanOrEqualTo(root.get("issueGroup").get("riskScore"), riskMax);
    }

    public static Specification<Report> recentReportedAtFrom(LocalDateTime from) {
        return (root, query, criteriaBuilder) -> from == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.greaterThanOrEqualTo(root.get("issueGroup").get("recentReportedAt"), from);
    }

    public static Specification<Report> recentReportedAtTo(LocalDateTime to) {
        return (root, query, criteriaBuilder) -> to == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.lessThanOrEqualTo(root.get("issueGroup").get("recentReportedAt"), to);
    }

    public static Specification<Report> issueGroupInBounds(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng) {
        return (root, query, criteriaBuilder) ->
            swLat == null || swLng == null || neLat == null || neLng == null
                ? criteriaBuilder.conjunction()
                : criteriaBuilder.and(
                    criteriaBuilder.between(root.get("issueGroup").get("groupLatitude"), swLat, neLat),
                    criteriaBuilder.between(root.get("issueGroup").get("groupLongitude"), swLng, neLng)
                );
    }

    public static Specification<Report> issueGroupWithinRadius(
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters) {
        return (root, query, criteriaBuilder) -> {
            if (latitude == null || longitude == null || radiusMeters == null) {
                return criteriaBuilder.conjunction();
            }

            Expression<Double> groupLatitude =
                root.get("issueGroup").get("groupLatitude").as(Double.class);
            Expression<Double> groupLongitude =
                root.get("issueGroup").get("groupLongitude").as(Double.class);
            Expression<Double> latitudeRadians =
                criteriaBuilder.function("radians", Double.class, groupLatitude);
            Expression<Double> longitudeRadians =
                criteriaBuilder.function("radians", Double.class, groupLongitude);
            Expression<Double> targetLatitudeRadians =
                criteriaBuilder.function("radians", Double.class, criteriaBuilder.literal(latitude.doubleValue()));
            Expression<Double> targetLongitudeRadians =
                criteriaBuilder.function("radians", Double.class, criteriaBuilder.literal(longitude.doubleValue()));

            Expression<Double> cosineDistance = criteriaBuilder.sum(
                criteriaBuilder.prod(
                    criteriaBuilder.function("sin", Double.class, targetLatitudeRadians),
                    criteriaBuilder.function("sin", Double.class, latitudeRadians)
                ),
                criteriaBuilder.prod(
                    criteriaBuilder.prod(
                        criteriaBuilder.function("cos", Double.class, targetLatitudeRadians),
                        criteriaBuilder.function("cos", Double.class, latitudeRadians)
                    ),
                    criteriaBuilder.function(
                        "cos",
                        Double.class,
                        criteriaBuilder.diff(longitudeRadians, targetLongitudeRadians)
                    )
                )
            );
            Expression<Double> clampedCosineDistance = criteriaBuilder.function(
                "greatest",
                Double.class,
                criteriaBuilder.literal(-1.0),
                criteriaBuilder.function(
                    "least",
                    Double.class,
                    criteriaBuilder.literal(1.0),
                    cosineDistance
                )
            );
            Expression<Double> distanceMeters = criteriaBuilder.prod(
                criteriaBuilder.literal(6371000.0),
                criteriaBuilder.function(
                    "acos",
                    Double.class,
                    clampedCosineDistance
                )
            );

            return criteriaBuilder.lessThanOrEqualTo(distanceMeters, radiusMeters.doubleValue());
        };
    }
}
