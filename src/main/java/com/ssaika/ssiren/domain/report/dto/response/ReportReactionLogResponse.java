package com.ssaika.ssiren.domain.report.dto.response;

import com.ssaika.ssiren.domain.report.entity.ReportReactionLog;
import com.ssaika.ssiren.global.enums.ReportReactionType;
import java.time.LocalDateTime;

public record ReportReactionLogResponse(
    Long id,
    ReportReactionType reactionType,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Long reportId,
    Long userId
) {

    public static ReportReactionLogResponse from(ReportReactionLog reactionLog) {
        return new ReportReactionLogResponse(
            reactionLog.getId(),
            reactionLog.getReactionType(),
            reactionLog.getCreatedAt(),
            reactionLog.getUpdatedAt(),
            reactionLog.getReport().getId(),
            reactionLog.getUser().getId()
        );
    }
}
