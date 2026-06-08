package com.ssaika.ssiren.global.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KakaoLocalProperties.class)
public class KakaoLocalWebClientConfig {

    private final KakaoLocalProperties kakaoLocalProperties;

    @Bean
    public WebClient kakaoLocalWebClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, kakaoLocalProperties.getConnectTimeoutMs())
            .responseTimeout(Duration.ofMillis(kakaoLocalProperties.getResponseTimeoutMs()));

        return WebClient.builder()
            .baseUrl(kakaoLocalProperties.getBaseUrl())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
