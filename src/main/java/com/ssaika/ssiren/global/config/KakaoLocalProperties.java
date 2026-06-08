package com.ssaika.ssiren.global.config;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "kakao.local")
public class KakaoLocalProperties {

    private String restApiKey;

    private String baseUrl;

    @Positive
    private int connectTimeoutMs;

    @Positive
    private int responseTimeoutMs;

    public boolean hasRestApiKey() {
        return restApiKey != null && !restApiKey.isBlank();
    }
}
