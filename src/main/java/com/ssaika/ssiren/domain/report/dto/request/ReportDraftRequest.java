package com.ssaika.ssiren.domain.report.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record ReportDraftRequest(
    List<MultipartFile> images,

    @NotBlank(message = "제보 내용은 필수입니다.")
    String content,

    @NotNull(message = "위도는 필수입니다.")
    BigDecimal latitude,

    @NotNull(message = "경도는 필수입니다.")
    BigDecimal longitude,

    LocalDateTime occurredAt
) {
}
