package com.ssaika.ssiren.global.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "ai.client")
public class AiClientProperties {

    @NotBlank
    private String baseUrl;

    @Positive
    private int connectTimeoutMs;

    @Positive
    private int responseTimeoutMs;
}
