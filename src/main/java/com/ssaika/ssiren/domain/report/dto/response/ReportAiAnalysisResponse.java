package com.ssaika.ssiren.domain.report.dto.response;

import com.ssaika.ssiren.domain.report.client.dto.response.ReportAiAnalyzeResponse;
import java.math.BigDecimal;
import java.util.List;

public record ReportAiAnalysisResponse(
    List<String> detectedObjects,
    FalseReport falseReport,
    EmergencyGuide emergencyGuide
) {

    public static ReportAiAnalysisResponse from(ReportAiAnalyzeResponse.Analysis analysis) {
        if (analysis == null) {
            return new ReportAiAnalysisResponse(
                List.of(),
                new FalseReport(false, BigDecimal.ZERO, null),
                new EmergencyGuide(false, null)
            );
        }

        return new ReportAiAnalysisResponse(
            analysis.detectedObjects() == null ? List.of() : analysis.detectedObjects(),
            FalseReport.from(analysis.falseReport()),
            EmergencyGuide.from(analysis.emergencyGuide())
        );
    }

    public record FalseReport(
        Boolean isSuspicious,
        BigDecimal score,
        String reason
    ) {

        private static FalseReport from(ReportAiAnalyzeResponse.FalseReport falseReport) {
            if (falseReport == null) {
                return new FalseReport(false, BigDecimal.ZERO, null);
            }
            return new FalseReport(
                falseReport.isSuspicious(),
                falseReport.score(),
                falseReport.reason()
            );
        }
    }

    public record EmergencyGuide(
        Boolean isEmergency,
        String message
    ) {

        private static EmergencyGuide from(ReportAiAnalyzeResponse.EmergencyGuide emergencyGuide) {
            if (emergencyGuide == null) {
                return new EmergencyGuide(false, null);
            }
            return new EmergencyGuide(
                emergencyGuide.isEmergency(),
                emergencyGuide.message()
            );
        }
    }
}
