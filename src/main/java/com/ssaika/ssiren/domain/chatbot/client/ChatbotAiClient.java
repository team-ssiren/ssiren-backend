package com.ssaika.ssiren.domain.chatbot.client;

import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotAnswerRequest;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotAnswerResponse;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotPlanRequest;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotPlanResponse;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotTitleRequest;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotTitleResponse;
import java.time.Duration;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Slf4j
@Component
public class ChatbotAiClient {

    private static final String PLAN_PATH = "/internal/v1/chatbot:plan";
    private static final String ANSWER_PATH = "/internal/v1/chatbot:answer";
    private static final String TITLE_PATH = "/internal/v1/chatbot:title";
    private static final int MAX_ATTEMPTS = 2;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

    private final RestClient restClient;

    public ChatbotAiClient(
        @Value("${AI_BASE_URL:http://localhost:8000}") String aiBaseUrl) {
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(aiBaseUrl);
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        this.restClient = RestClient.builder()
            .uriBuilderFactory(uriBuilderFactory)
            .requestFactory(createRequestFactory())
            .build();
    }

    public Optional<ChatbotPlanResponse> requestPlan(ChatbotPlanRequest request) {
        return requestWithRetry(PLAN_PATH, request, ChatbotPlanResponse.class);
    }

    public Optional<ChatbotAnswerResponse> requestAnswer(ChatbotAnswerRequest request) {
        return requestWithRetry(ANSWER_PATH, request, ChatbotAnswerResponse.class);
    }

    public Optional<ChatbotTitleResponse> requestTitle(ChatbotTitleRequest request) {
        return requestWithRetry(TITLE_PATH, request, ChatbotTitleResponse.class);
    }

    private <T> Optional<T> requestWithRetry(String path, Object request, Class<T> responseType) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                T response = restClient.post()
                    .uri(path)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(responseType);
                return Optional.ofNullable(response);
            } catch (RestClientException exception) {
                log.warn("Chatbot AI request failed. path={}, attempt={}", path, attempt, exception);
            }
        }

        return Optional.empty();
    }

    private SimpleClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        return requestFactory;
    }
}
