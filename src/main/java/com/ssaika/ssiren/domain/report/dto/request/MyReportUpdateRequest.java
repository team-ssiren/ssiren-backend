package com.ssaika.ssiren.domain.report.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssaika.ssiren.global.enums.ReportVisibility;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MyReportUpdateRequest(
    @Pattern(regexp = ".*\\S.*", message = "제보 제목은 공백일 수 없습니다.")
    @Size(max = 150, message = "제보 제목은 150자 이하여야 합니다.")
    String title,
    JsonNode contents,
    ReportVisibility visibility,
    Long categoryId
) {
}
