package com.ssaika.ssiren.domain.report.dto.response;

import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.global.enums.IssueGroupStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReportIssueGroupResponse(
    Long id,
    String title,
    String content,
    BigDecimal groupLatitude,
    BigDecimal groupLongitude,
    Integer reportCount,
    Integer yesCount,
    Integer noCount,
    Integer unknownCount,
    LocalDateTime recentReportedAt,
    IssueGroupStatus status,
    BigDecimal riskScore,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    public static ReportIssueGroupResponse from(IssueGroup issueGroup) {
        return new ReportIssueGroupResponse(
            issueGroup.getId(),
            issueGroup.getTitle(),
            issueGroup.getContent(),
            issueGroup.getGroupLatitude(),
            issueGroup.getGroupLongitude(),
            issueGroup.getReportCount(),
            issueGroup.getYesCount(),
            issueGroup.getNoCount(),
            issueGroup.getUnknownCount(),
            issueGroup.getRecentReportedAt(),
            issueGroup.getStatus(),
            issueGroup.getRiskScore(),
            issueGroup.getCreatedAt(),
            issueGroup.getUpdatedAt()
        );
    }
}
