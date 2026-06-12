package com.ssaika.ssiren.domain.report.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssaika.ssiren.global.enums.ReportVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ReportCreateRequest(
    @NotBlank(message = "제보 제목은 필수입니다.")
    String title,

    @NotNull(message = "제보 본문은 필수입니다.")
    JsonNode contents,

    @NotNull(message = "위도는 필수입니다.")
    BigDecimal latitude,

    @NotNull(message = "경도는 필수입니다.")
    BigDecimal longitude,

    @NotBlank(message = "도로명 주소는 필수입니다.")
    String roadAddress,

    @NotBlank(message = "지번 주소는 필수입니다.")
    String jibunAddress,

    @NotBlank(message = "시/도는 필수입니다.")
    String sido,

    @NotBlank(message = "시/군/구는 필수입니다.")
    String sigungu,

    @NotBlank(message = "읍/면/동은 필수입니다.")
    String eupmyeondong,

    @NotNull(message = "발생 시각은 필수입니다.")
    LocalDateTime occurredAt,

    @NotNull(message = "위험 점수는 필수입니다.")
    BigDecimal riskScore,

    @NotBlank(message = "부서 배정 근거는 필수입니다.")
    String assignmentReason,

    @NotNull(message = "공개 범위는 필수입니다.")
    ReportVisibility visibility,

    @NotNull(message = "카테고리는 필수입니다.")
    Long categoryId,

    @NotNull(message = "담당 부서는 필수입니다.")
    Long departmentId,

    Long issueGroupId,

    List<BigDecimal> embedding
) {
}
