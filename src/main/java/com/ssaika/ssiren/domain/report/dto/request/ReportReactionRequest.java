package com.ssaika.ssiren.domain.report.dto.request;

import com.ssaika.ssiren.global.enums.ReportReactionType;
import jakarta.validation.constraints.NotNull;

public record ReportReactionRequest(
    @NotNull(message = "반응 유형은 필수입니다.")
    ReportReactionType reactionType
) {
}
