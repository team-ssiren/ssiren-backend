package com.ssaika.ssiren.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloudflare.r2")
public record R2Properties(
    String endpoint,
    String accessKeyId,
    String secretAccessKey,
    String bucket,
    String publicUrl,
    String region
) {

    public String resolvedRegion() {
        return region == null || region.isBlank() ? "auto" : region;
    }

    public void validateRequired() {
        validateNotBlank(endpoint, "cloudflare.r2.endpoint");
        validateNotBlank(accessKeyId, "cloudflare.r2.access-key-id");
        validateNotBlank(secretAccessKey, "cloudflare.r2.secret-access-key");
        validateNotBlank(bucket, "cloudflare.r2.bucket");
        validateNotBlank(publicUrl, "cloudflare.r2.public-url");
    }

    private void validateNotBlank(String value, String propertyName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(propertyName + " 설정이 필요합니다.");
        }
    }
}
