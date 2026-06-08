package com.ssaika.ssiren.domain.report.client;

import com.ssaika.ssiren.domain.report.client.dto.request.ReportAiAnalyzeRequest;
import com.ssaika.ssiren.domain.report.client.dto.response.ReportAiAnalyzeResponse;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
public class ReportAiHttpClient implements ReportAiClient {

    private static final String ANALYZE_URI = "/internal/v1/reports:analyze";
    private static final String IMAGE_PART_NAME = "images";
    private static final int MAX_IMAGE_COUNT = 5;
    private static final MediaType DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_OCTET_STREAM;

    private final WebClient aiWebClient;

    public ReportAiHttpClient(@Qualifier("aiWebClient") WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    @Override
    public ReportAiAnalyzeResponse analyzeReport(ReportAiAnalyzeRequest request) {
        try {
            MultipartBodyBuilder builder = new MultipartBodyBuilder();
            builder.part("content", request.content());
            builder.part("latitude", request.latitude());
            builder.part("longitude", request.longitude());
            builder.part("occurredAt", request.occurredAt().toString());
            addOptionalPart(builder, "roadAddress", request.roadAddress());
            addOptionalPart(builder, "sido", request.sido());
            addOptionalPart(builder, "sigungu", request.sigungu());
            addOptionalPart(builder, "eupmyeondong", request.eupmyeondong());
            addImages(builder, request.images());

            ReportAiAnalyzeResponse response = aiWebClient.post()
                .uri(ANALYZE_URI)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .bodyValue(builder.build())
                .retrieve()
                .bodyToMono(ReportAiAnalyzeResponse.class)
                .block();

            return resolveResponse(response);
        } catch (RuntimeException e) {
            throw mapException(e);
        }
    }

    private void addOptionalPart(MultipartBodyBuilder builder, String name, String value) {
        if (value != null && !value.isBlank()) {
            builder.part(name, value);
        }
    }

    private void addImages(MultipartBodyBuilder builder, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return;
        }
        if (images.size() > MAX_IMAGE_COUNT) {
            throw new CustomException("제보 이미지는 최대 5장까지 첨부할 수 있습니다.", ErrorCode.INVALID_PARAMETER);
        }

        images.stream()
            .filter(image -> image != null && !image.isEmpty())
            .forEach(image -> builder
                .part(IMAGE_PART_NAME, createImageResource(image))
                .filename(resolveFileName(image))
                .contentType(resolveContentType(image)));
    }

    private ByteArrayResource createImageResource(MultipartFile image) {
        try {
            byte[] bytes = image.getBytes();
            return new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    return resolveFileName(image);
                }
            };
        } catch (IOException e) {
            throw new CustomException("제보 이미지 파일을 읽을 수 없습니다.", ErrorCode.INVALID_FORMAT);
        }
    }

    private String resolveFileName(MultipartFile image) {
        String fileName = image.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            return "report-image";
        }
        return fileName;
    }

    private MediaType resolveContentType(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null || contentType.isBlank()) {
            return DEFAULT_CONTENT_TYPE;
        }
        return MediaType.parseMediaType(contentType);
    }

    private ReportAiAnalyzeResponse resolveResponse(ReportAiAnalyzeResponse response) {
        if (response == null || response.title() == null || response.category() == null) {
            throw new CustomException(ErrorCode.AI_SERVER_RESPONSE_ERROR.getMessage(), ErrorCode.AI_SERVER_RESPONSE_ERROR);
        }
        return response;
    }

    private CustomException mapException(Throwable e) {
        if (e instanceof CustomException customException) {
            return customException;
        }
        if (e instanceof WebClientResponseException responseException) {
            log.warn(
                "AI report analyze response error. status={}, body={}",
                responseException.getStatusCode(),
                responseException.getResponseBodyAsString()
            );
            return new CustomException(ErrorCode.AI_SERVER_RESPONSE_ERROR.getMessage(), ErrorCode.AI_SERVER_RESPONSE_ERROR);
        }
        if (isTimeout(e)) {
            return new CustomException(ErrorCode.AI_SERVER_TIMEOUT.getMessage(), ErrorCode.AI_SERVER_TIMEOUT);
        }
        if (e instanceof WebClientRequestException requestException) {
            log.warn("AI report analyze request failed. uri={}, message={}",
                requestException.getUri(),
                requestException.getMessage()
            );
            return new CustomException(
                ErrorCode.AI_SERVER_CONNECTION_FAILED.getMessage(),
                ErrorCode.AI_SERVER_CONNECTION_FAILED
            );
        }
        return new CustomException(ErrorCode.AI_SERVER_RESPONSE_ERROR.getMessage(), ErrorCode.AI_SERVER_RESPONSE_ERROR);
    }

    private boolean isTimeout(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof TimeoutException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
