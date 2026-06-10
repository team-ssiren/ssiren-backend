package com.ssaika.ssiren.domain.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IssueGroupStatsService {

    private static final BigDecimal EARTH_RADIUS_METERS = BigDecimal.valueOf(6_371_000);

    private final ObjectMapper objectMapper;

    // 자동 병합 시 사용
    public void refreshIssueGroupByReports(IssueGroup issueGroup, List<Report> reports) {
        validateReports(reports);

        Report representativeReport = normalizeRepresentativeReport(reports);
        refreshIssueGroupByRepresentative(issueGroup, reports, representativeReport);
    }

    // 수동 병합 시 사용
    public void refreshIssueGroupByReports(
            IssueGroup issueGroup,
            List<Report> reports,
            Report preferredRepresentativeReport
    ) {
        validateReports(reports);

        Report representativeReport = normalizeRepresentativeReport(reports, preferredRepresentativeReport);
        refreshIssueGroupByRepresentative(issueGroup, reports, representativeReport);
    }

    public BigDecimal calculateGroupDiameter(List<Report> reports) {
        validateReports(reports);

        if (reports.size() <= 1) {
            return BigDecimal.ZERO;
        }

        BigDecimal maxDistance = BigDecimal.ZERO;
        for (int i = 0; i < reports.size(); i++) {
            for (int j = i + 1; j < reports.size(); j++) {
                Report first = reports.get(i);
                Report second = reports.get(j);
                BigDecimal distance = calculateDistanceMeters(
                        first.getLatitude(),
                        first.getLongitude(),
                        second.getLatitude(),
                        second.getLongitude()
                );
                maxDistance = maxDistance.max(distance);
            }
        }
        return maxDistance;
    }

    private void refreshIssueGroupByRepresentative(
            IssueGroup issueGroup,
            List<Report> reports,
            Report representativeReport
    ) {
        Coordinate center = calculateGroupCenter(reports);

        issueGroup.refreshStats(
                representativeReport.getTitle(),
                resolveIssueGroupContent(parseReportContents(representativeReport)),
                reports.size(),
                center.latitude(),
                center.longitude(),
                calculateGroupDiameter(reports),
                calculateMaxRiskScore(reports),
                calculateRecentReportedAt(reports)
        );
    }

    private Report normalizeRepresentativeReport(List<Report> reports) {
        Report representativeReport = reports.stream()
                .filter(report -> Boolean.TRUE.equals(report.getIsRepresentative()))
                .max(Comparator.comparing(this::reportReportedAt))
                .orElseGet(() -> reports.stream()
                        .max(Comparator.comparing(this::reportReportedAt))
                        .orElseThrow(() -> new CustomException(
                                "이슈 그룹 대표 제보를 찾을 수 없습니다.",
                                ErrorCode.NOT_FOUND
                        )));

        markOnlyRepresentative(reports, representativeReport);
        return representativeReport;
    }

    private Report normalizeRepresentativeReport(
            List<Report> reports,
            Report preferredRepresentativeReport
    ) {
        if (preferredRepresentativeReport != null
                && reports.stream().anyMatch(report -> Objects.equals(report.getId(), preferredRepresentativeReport.getId()))) {
            markOnlyRepresentative(reports, preferredRepresentativeReport);
            return preferredRepresentativeReport;
        }

        return normalizeRepresentativeReport(reports);
    }

    private void markOnlyRepresentative(List<Report> reports, Report representativeReport) {
        for (Report report : reports) {
            if (Objects.equals(report.getId(), representativeReport.getId())) {
                report.markRepresentative();
                continue;
            }
            report.unmarkRepresentative();
        }
    }

    private Coordinate calculateGroupCenter(List<Report> reports) {
        BigDecimal latitudeSum = BigDecimal.ZERO;
        BigDecimal longitudeSum = BigDecimal.ZERO;

        for (Report report : reports) {
            latitudeSum = latitudeSum.add(report.getLatitude());
            longitudeSum = longitudeSum.add(report.getLongitude());
        }

        BigDecimal count = BigDecimal.valueOf(reports.size());
        return new Coordinate(
                latitudeSum.divide(count, 7, RoundingMode.HALF_UP),
                longitudeSum.divide(count, 7, RoundingMode.HALF_UP)
        );
    }

    private BigDecimal calculateDistanceMeters(
            BigDecimal latitude1,
            BigDecimal longitude1,
            BigDecimal latitude2,
            BigDecimal longitude2
    ) {
        double lat1 = Math.toRadians(latitude1.doubleValue());
        double lat2 = Math.toRadians(latitude2.doubleValue());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(longitude2.doubleValue() - longitude1.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS.multiply(BigDecimal.valueOf(c)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMaxRiskScore(List<Report> reports) {
        return reports.stream()
                .map(Report::getRiskScore)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private LocalDateTime calculateRecentReportedAt(List<Report> reports) {
        return reports.stream()
                .map(this::reportReportedAt)
                .max(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());
    }

    private LocalDateTime reportReportedAt(Report report) {
        return report.getCreatedAt() == null ? report.getOccurredAt() : report.getCreatedAt();
    }

    private JsonNode parseReportContents(Report report) {
        try {
            return objectMapper.readTree(report.getContents());
        } catch (JsonProcessingException e) {
            throw new CustomException("제보 본문 형식이 올바르지 않습니다.", ErrorCode.INVALID_FORMAT);
        }
    }

    private String resolveIssueGroupContent(JsonNode contents) {
        JsonNode summary = contents == null ? null : contents.get("summary");
        if (summary != null && summary.isTextual() && !summary.asText().isBlank()) {
            return summary.asText();
        }
        return contents == null ? null : contents.toString();
    }

    private void validateReports(List<Report> reports) {
        if (reports == null || reports.isEmpty()) {
            throw new CustomException("이슈 그룹 통계를 계산할 제보가 없습니다.", ErrorCode.BAD_REQUEST);
        }
    }

    private record Coordinate(
            BigDecimal latitude,
            BigDecimal longitude
    ) {
    }
}