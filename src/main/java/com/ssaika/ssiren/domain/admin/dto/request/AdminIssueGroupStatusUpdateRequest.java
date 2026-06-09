package com.ssaika.ssiren.domain.admin.dto.request;

import com.ssaika.ssiren.global.enums.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdminIssueGroupStatusUpdateRequest(
        @NotNull
        ReportStatus status,

        @NotBlank
        String reason,

        Boolean notifyReporter
) {

    public boolean shouldNotifyReporter() {
        return notifyReporter == null || Boolean.TRUE.equals(notifyReporter);
    }
}