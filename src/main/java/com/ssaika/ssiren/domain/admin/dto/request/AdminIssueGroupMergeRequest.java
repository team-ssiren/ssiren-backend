package com.ssaika.ssiren.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AdminIssueGroupMergeRequest(
        @NotEmpty
        List<Long> sourceIssueGroupIds,

        @NotBlank
        String reason,

        Boolean notifyReporter
) {

    public boolean shouldNotifyReporter() {
        return Boolean.TRUE.equals(notifyReporter);
    }
}