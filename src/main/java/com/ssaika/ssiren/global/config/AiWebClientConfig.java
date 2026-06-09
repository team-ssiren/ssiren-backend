package com.ssaika.ssiren.global.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AiClientProperties.class)
public class AiWebClientConfig {

    private static final int MAX_AI_RESPONSE_IN_MEMORY_SIZE = 20 * 1024 * 1024;

    private final AiClientProperties aiClientProperties;

    @Bean
    public WebClient aiWebClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, aiClientProperties.getConnectTimeoutMs())
            .responseTimeout(Duration.ofMillis(aiClientProperties.getResponseTimeoutMs()));

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs()
                .maxInMemorySize(MAX_AI_RESPONSE_IN_MEMORY_SIZE))
            .build();

        return WebClient.builder()
            .baseUrl(aiClientProperties.getBaseUrl())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(exchangeStrategies)
            .build();
    }
}
