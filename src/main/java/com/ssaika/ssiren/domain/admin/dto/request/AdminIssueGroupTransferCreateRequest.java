package com.ssaika.ssiren.domain.admin.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminIssueGroupTransferCreateRequest(
        @NotNull(message = "이관 대상 부서 ID는 필수입니다.")
        Long targetDepartmentId,

        @NotBlank(message = "이관 요청 사유는 필수입니다.")
        @Size(max = 500, message = "이관 요청 사유는 500자를 초과할 수 없습니다.")
        String requestReason
) {
}