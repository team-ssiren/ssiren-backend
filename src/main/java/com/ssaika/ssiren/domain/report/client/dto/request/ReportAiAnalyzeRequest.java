package com.ssaika.ssiren.domain.report.client.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public record ReportAiAnalyzeRequest(
    String content,
    BigDecimal latitude,
    BigDecimal longitude,
    LocalDateTime occurredAt,
    String roadAddress,
    String sido,
    String sigungu,
    String eupmyeondong,
    List<MultipartFile> images
) {
}
