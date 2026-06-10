package com.ssaika.ssiren.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminIssueGroupTransferResponseRequest(
        @NotBlank(message = "이관 응답 사유는 필수입니다.")
        @Size(max = 500, message = "이관 응답 사유는 500자를 초과할 수 없습니다.")
        String responseReason
) {
}