package com.ssaika.ssiren.domain.chatbot.client.dto;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.Report;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ChatbotReportContext(
    Long reportId,
    String title,
    String summary,
    String category,
    String address,
    BigDecimal riskScore,
    BigDecimal distanceMeters,
    LocalDateTime recentReportedAt
) {

    public static ChatbotReportContext of(
        Report report,
        BigDecimal distanceMeters,
        ObjectMapper objectMapper) {
        return new ChatbotReportContext(
            report.getId(),
            report.getTitle(),
            resolveSummary(report, objectMapper),
            report.getCategory().getCategoryName(),
            report.getRoadAddress(),
            report.getIssueGroup().getRiskScore(),
            distanceMeters,
            report.getIssueGroup().getRecentReportedAt()
        );
    }

    private static String resolveSummary(Report report, ObjectMapper objectMapper) {
        try {
            JsonNode contents = objectMapper.readTree(report.getContents());
            JsonNode summary = contents.get("summary");
            if (summary != null && !summary.isNull()) {
                return summary.asText();
            }
        } catch (Exception ignored) {
            return report.getContents();
        }

        return report.getTitle();
    }
}
