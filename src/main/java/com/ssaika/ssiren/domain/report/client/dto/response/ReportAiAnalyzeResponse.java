package com.ssaika.ssiren.domain.report.client.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReportAiAnalyzeResponse(
    String title,
    JsonNode contents,
    List<String> keywords,
    Category category,
    BigDecimal riskScore,
    Analysis analysis,
    LocalDateTime occurredAt,
    List<BigDecimal> embedding
) {

    public record Category(
        String categoryCode,
        BigDecimal confidence
    ) {
    }

    public record Analysis(
        List<String> detectedObjects,
        FalseReport falseReport,
        EmergencyGuide emergencyGuide
    ) {
    }

    public record FalseReport(
        Boolean isSuspicious,
        BigDecimal score,
        String reason
    ) {
    }

    public record EmergencyGuide(
        Boolean isEmergency,
        String message
    ) {
    }
}
