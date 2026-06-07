package com.ssaika.ssiren.domain.report.dto.response;

import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import com.ssaika.ssiren.global.enums.ReportStatus;
import java.time.LocalDateTime;

public record ReportStatusHistoryResponse(
    Long id,
    ReportStatus previousStatus,
    ReportStatus newStatus,
    String reason,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long reportId,
    Long userId
) {

    public static ReportStatusHistoryResponse from(ReportStatusHistory statusHistory) {
        return new ReportStatusHistoryResponse(
            statusHistory.getId(),
            statusHistory.getPreviousStatus(),
            statusHistory.getNewStatus(),
            statusHistory.getReason(),
            statusHistory.getCreatedAt(),
            statusHistory.getUpdatedAt(),
            statusHistory.getReport().getId(),
            statusHistory.getUser() == null ? null : statusHistory.getUser().getId()
        );
    }
}
