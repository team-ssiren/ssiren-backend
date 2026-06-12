package com.ssaika.ssiren.domain.report.dto.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.global.enums.ReportStatus;
import com.ssaika.ssiren.global.enums.ReportVisibility;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReportResponse(
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
    String assignmentReason,
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

    public static ReportResponse from(Report report, ObjectMapper objectMapper) {
        return new ReportResponse(
            report.getId(),
            report.getTitle(),
            parseContents(report.getContents(), objectMapper),
            report.getLatitude(),
            report.getLongitude(),
            report.getRoadAddress(),
            report.getJibunAddress(),
            report.getSido(),
            report.getSigungu(),
            report.getEupmyeondong(),
            report.getOccurredAt(),
            report.getRiskScore(),
            report.getAssignmentReason(),
            report.getStatus(),
            report.getIsRepresentative(),
            report.getVisibility(),
            report.getIsDeleted(),
            report.getCreatedAt(),
            report.getUpdatedAt(),
            report.getUser().getId(),
            report.getCategory().getId(),
            report.getIssueGroup().getId(),
            report.getDepartment().getId()
        );
    }

    private static JsonNode parseContents(String contents, ObjectMapper objectMapper) {
        try {
            return objectMapper.readTree(contents);
        } catch (Exception e) {
            return objectMapper.getNodeFactory().textNode(contents);
        }
    }
}
