package com.ssaika.ssiren.domain.report.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssaika.ssiren.global.enums.ReportStatus;
import com.ssaika.ssiren.global.enums.ReportVisibility;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReportDraftResponse(
    Long id,
    String title,
    JsonNode contents,
    BigDecimal latitude,
    BigDecimal longitude,
    String roadAddress,
    String jibunAddress,
    String sido,
    String sigungu,
    String eupmyeondong,
    LocalDateTime occurredAt,
    BigDecimal riskScore,
    ReportStatus status,
    Boolean isRepresentative,
    ReportVisibility visibility,
    Boolean isDeleted,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long userId,
    Long categoryId,
    Long issueGroupId,
    Long departmentId
) {
}
