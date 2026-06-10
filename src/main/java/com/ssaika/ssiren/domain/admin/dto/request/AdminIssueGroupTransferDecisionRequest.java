package com.ssaika.ssiren.domain.admin.dto.request;

import com.ssaika.ssiren.domain.report.entity.IssueGroupTransferHistoryStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminIssueGroupTransferDecisionRequest(
        @NotNull(message = "이관 응답 상태는 필수입니다.")
        IssueGroupTransferHistoryStatus status,

        @NotBlank(message = "이관 응답 사유는 필수입니다.")
        @Size(max = 500, message = "이관 응답 사유는 500자를 초과할 수 없습니다.")
        String responseReason
) {

    public boolean isAccepted() {
        return status == IssueGroupTransferHistoryStatus.ACCEPTED;
    }

    public boolean isRejected() {
        return status == IssueGroupTransferHistoryStatus.REJECTED;
    }
}