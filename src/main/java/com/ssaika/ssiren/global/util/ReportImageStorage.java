package com.ssaika.ssiren.global.util;

import com.ssaika.ssiren.global.config.R2Properties;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportImageStorage {

    private static final String DEFAULT_CONTENT_TYPE = "image/jpeg";
    private static final String REPORT_IMAGE_PREFIX = "reports";

    private final S3Client r2S3Client;
    private final R2Properties r2Properties;

    public UploadedReportImage upload(Long userId, Long reportId, MultipartFile image) {
        validateConfigured();

        String objectKey = createObjectKey(userId, reportId, image);
        try {
            r2S3Client.putObject(
                PutObjectRequest.builder()
                    .bucket(r2Properties.bucket())
                    .key(objectKey)
                    .contentType(resolveContentType(image))
                    .contentLength(image.getSize())
                    .build(),
                RequestBody.fromBytes(image.getBytes())
            );

            return new UploadedReportImage(objectKey, createPublicUrl(objectKey));
        } catch (IOException | RuntimeException e) {
            throw new CustomException("제보 이미지 업로드에 실패했습니다.", ErrorCode.REPORT_IMAGE_UPLOAD_FAILED);
        }
    }

    public void deleteQuietly(String objectKey) {
        if (isBlank(objectKey)) {
            return;
        }
        try {
            r2S3Client.deleteObject(builder -> builder
                .bucket(r2Properties.bucket())
                .key(objectKey)
            );
        } catch (RuntimeException e) {
            log.warn("Failed to delete uploaded report image. objectKey={}", objectKey);
        }
    }

    public void deleteByImageUrlQuietly(String imageUrl) {
        resolveObjectKey(imageUrl).ifPresent(this::deleteQuietly);
    }

    private void validateConfigured() {
        if (isBlank(r2Properties.endpoint())
            || isBlank(r2Properties.accessKeyId())
            || isBlank(r2Properties.secretAccessKey())
            || isBlank(r2Properties.bucket())
            || isBlank(r2Properties.publicUrl())) {
            throw new CustomException("이미지 저장소 설정이 누락되었습니다.", ErrorCode.REPORT_IMAGE_UPLOAD_FAILED);
        }
    }

    private String createObjectKey(Long userId, Long reportId, MultipartFile image) {
        String extension = resolveExtension(image.getOriginalFilename(), image.getContentType());
        return REPORT_IMAGE_PREFIX
            + "/"
            + userId
            + "/"
            + reportId
            + "/"
            + UUID.randomUUID()
            + extension;
    }

    private String resolveExtension(String originalFilename, String contentType) {
        if (originalFilename != null) {
            int dotIndex = originalFilename.lastIndexOf('.');
            if (dotIndex >= 0 && dotIndex < originalFilename.length() - 1) {
                return "." + originalFilename.substring(dotIndex + 1).toLowerCase();
            }
        }
        if ("image/png".equals(contentType)) {
            return ".png";
        }
        if ("image/webp".equals(contentType)) {
            return ".webp";
        }
        return ".jpg";
    }

    private String resolveContentType(MultipartFile image) {
        String contentType = image.getContentType();
        return isBlank(contentType) ? DEFAULT_CONTENT_TYPE : contentType;
    }

    private String createPublicUrl(String objectKey) {
        String baseUrl = r2Properties.publicUrl().replaceAll("/+$", "");
        return baseUrl + "/" + URLEncoder.encode(objectKey, StandardCharsets.UTF_8)
            .replace("%2F", "/");
    }

    private Optional<String> resolveObjectKey(String imageUrl) {
        if (isBlank(imageUrl) || isBlank(r2Properties.publicUrl())) {
            return Optional.empty();
        }

        String baseUrl = r2Properties.publicUrl().replaceAll("/+$", "");
        String prefix = baseUrl + "/";
        if (!imageUrl.startsWith(prefix)) {
            return Optional.empty();
        }

        String encodedObjectKey = imageUrl.substring(prefix.length());
        if (encodedObjectKey.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(URLDecoder.decode(encodedObjectKey, StandardCharsets.UTF_8));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record UploadedReportImage(String objectKey, String imageUrl) {
    }
}
