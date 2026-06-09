package com.ssaika.ssiren.domain.chatbot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.chatbot.client.ChatbotAiClient;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotAction;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotAnswerContext;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotAnswerRequest;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotAnswerResponse;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotHistoryMessage;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotPlanParams;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotPlanRequest;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotPlanResponse;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotReportContext;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotUserLocation;
import com.ssaika.ssiren.domain.chatbot.dto.request.ChatbotMessageSendRequest;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotMessageCursorResponse;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotMessageResponse;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotMessageSendResponse;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotSessionResponse;
import com.ssaika.ssiren.domain.chatbot.entity.ChatbotMessage;
import com.ssaika.ssiren.domain.chatbot.entity.ChatbotSession;
import com.ssaika.ssiren.domain.chatbot.repository.ChatbotMessageRepository;
import com.ssaika.ssiren.domain.chatbot.repository.ChatbotSessionRepository;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportCategory;
import com.ssaika.ssiren.domain.report.repository.ReportCategoryRepository;
import com.ssaika.ssiren.domain.report.repository.ReportRepository;
import com.ssaika.ssiren.domain.report.repository.ReportSpecification;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.enums.ChatbotSenderType;
import com.ssaika.ssiren.global.enums.ReportVisibility;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotService {

    private static final String NEW_CHAT_TITLE = "새 대화";
    private static final String AI_FAILURE_MESSAGE = "현재 챗봇 응답을 생성할 수 없습니다.";
    private static final int HISTORY_SIZE = 10;
    private static final int CONTEXT_REPORT_SIZE = 5;

    private final ChatbotAiClient chatbotAiClient;
    private final ChatbotMessageRepository chatbotMessageRepository;
    private final ChatbotSessionRepository chatbotSessionRepository;
    private final ReportCategoryRepository reportCategoryRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public Page<ChatbotSessionResponse> getMyChatbotSessions(Long userId, Pageable pageable) {
        userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                ErrorCode.USER_NOT_FOUND));

        return chatbotSessionRepository.findAllByUser_Id(userId, pageable)
            .map(ChatbotSessionResponse::from);
    }

    public ChatbotMessageCursorResponse getChatbotMessages(Long userId, Long sessionId, Long cursor,
        Integer size) {
        chatbotSessionRepository.findByIdAndUser_Id(sessionId, userId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND.getMessage(),
                ErrorCode.NOT_FOUND));

        int requestSize = size + 1;
        Pageable pageable = PageRequest.of(0, requestSize);
        List<ChatbotMessage> messages = cursor == null
            ? chatbotMessageRepository.findAllBySession_IdOrderByIdDesc(sessionId, pageable)
            : chatbotMessageRepository.findAllBySession_IdAndIdLessThanOrderByIdDesc(sessionId,
                cursor, pageable);
        boolean hasNext = messages.size() > size;
        List<ChatbotMessageResponse> content = messages.stream()
            .limit(size)
            .map(ChatbotMessageResponse::from)
            .toList();

        return ChatbotMessageCursorResponse.of(content, hasNext);
    }

    @Transactional
    public ChatbotMessageSendResponse saveChatbotMessage(
        Long userId,
        Long sessionId,
        ChatbotMessageSendRequest request) {
        ChatbotSession session = getSessionForSend(userId, sessionId);
        Long messageCount = chatbotMessageRepository.countBySession_Id(sessionId);
        LocalDateTime now = LocalDateTime.now();
        List<ChatbotHistoryMessage> history = getRecentHistory(sessionId);
        String botAnswer = resolveBotAnswer(userId, request, history)
            .orElseThrow(() -> new CustomException(AI_FAILURE_MESSAGE,
                ErrorCode.INTERNAL_SERVER_ERROR));
        ChatbotMessage userMessage = chatbotMessageRepository.save(ChatbotMessage.create(
            session,
            ChatbotSenderType.USER,
            request.message(),
            now
        ));
        ChatbotMessage botMessage = chatbotMessageRepository.save(ChatbotMessage.create(
            session,
            ChatbotSenderType.BOT,
            botAnswer,
            LocalDateTime.now()
        ));

        if (messageCount == 0 && NEW_CHAT_TITLE.equals(session.getTitle())) {
            // TODO: AI 제목 생성 API 명세를 받으면 이 위치에서 10자 이내 제목 생성 후 session.updateTitle(...) 호출
        }

        return ChatbotMessageSendResponse.of(
            ChatbotSessionResponse.from(session),
            List.of(ChatbotMessageResponse.from(userMessage), ChatbotMessageResponse.from(botMessage))
        );
    }

    private ChatbotSession getSessionForSend(Long userId, Long sessionId) {
        ChatbotSession session = chatbotSessionRepository.findById(sessionId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND.getMessage(),
                ErrorCode.NOT_FOUND));
        if (!session.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN.getMessage(), ErrorCode.FORBIDDEN);
        }

        return session;
    }

    private List<ChatbotHistoryMessage> getRecentHistory(Long sessionId) {
        return chatbotMessageRepository.findAllBySession_IdOrderByIdDesc(
                sessionId,
                PageRequest.of(0, HISTORY_SIZE)
            )
            .stream()
            .sorted(Comparator.comparing(ChatbotMessage::getId))
            .map(ChatbotHistoryMessage::from)
            .toList();
    }

    private Optional<String> resolveBotAnswer(
        Long userId,
        ChatbotMessageSendRequest request,
        List<ChatbotHistoryMessage> history) {
        return chatbotAiClient.requestPlan(new ChatbotPlanRequest(request.message(), history))
            .flatMap(plan -> resolveBotAnswer(userId, request, history, plan));
    }

    private Optional<String> resolveBotAnswer(
        Long userId,
        ChatbotMessageSendRequest request,
        List<ChatbotHistoryMessage> history,
        ChatbotPlanResponse plan) {
        if (plan.action() == null) {
            return Optional.empty();
        }
        if (plan.action() == ChatbotAction.ANSWER_DIRECT) {
            return plan.answer() == null || plan.answer().isBlank()
                ? Optional.empty()
                : Optional.of(plan.answer());
        }

        ChatbotAnswerContext context = createAnswerContext(userId, plan, request);
        ChatbotAnswerRequest answerRequest = new ChatbotAnswerRequest(
            request.message(),
            history,
            context
        );

        return chatbotAiClient.requestAnswer(answerRequest)
            .map(ChatbotAnswerResponse::answer)
            .filter(answer -> !answer.isBlank());
    }

    private ChatbotAnswerContext createAnswerContext(
        Long userId,
        ChatbotPlanResponse plan,
        ChatbotMessageSendRequest request) {
        ChatbotAction action = plan.action();
        ChatbotPlanParams params = plan.params() == null ? new ChatbotPlanParams(null, null) : plan.params();
        return switch (action) {
            case SEARCH_NEARBY -> new ChatbotAnswerContext(
                action.name(),
                getNearbyReportContexts(params, request),
                new ChatbotUserLocation(request.latitude(), request.longitude())
            );
            case MY_REPORTS -> new ChatbotAnswerContext(
                action.name(),
                getMyReportContexts(userId),
                null
            );
            case ANSWER_DIRECT -> throw new CustomException(AI_FAILURE_MESSAGE,
                ErrorCode.INTERNAL_SERVER_ERROR);
        };
    }

    private List<ChatbotReportContext> getNearbyReportContexts(
        ChatbotPlanParams params,
        ChatbotMessageSendRequest request) {
        Long categoryId = params.categoryCode() == null
            ? null
            : reportCategoryRepository.findByCategoryCode(params.categoryCode())
                .map(ReportCategory::getId)
                .orElse(null);
        Specification<Report> specification = ReportSpecification.isRepresentative()
            .and(ReportSpecification.hasVisibility(ReportVisibility.PUBLIC))
            .and(ReportSpecification.isNotDeleted())
            .and(ReportSpecification.hasCategory(categoryId))
            .and(ReportSpecification.issueGroupWithinRadius(
                request.latitude(),
                request.longitude(),
                params.resolveRadiusMeters()
            ));

        return reportRepository.findAll(
                specification,
                PageRequest.of(0, CONTEXT_REPORT_SIZE,
                    Sort.by(Sort.Direction.DESC, "issueGroup.riskScore"))
            )
            .stream()
            .map(report -> ChatbotReportContext.of(
                report,
                calculateDistanceMeters(
                    request.latitude(),
                    request.longitude(),
                    report.getIssueGroup().getGroupLatitude(),
                    report.getIssueGroup().getGroupLongitude()
                ),
                objectMapper
            ))
            .toList();
    }

    private List<ChatbotReportContext> getMyReportContexts(Long userId) {
        Specification<Report> specification = ReportSpecification.belongsToUser(userId)
            .and(ReportSpecification.isNotDeleted());

        return reportRepository.findAll(
                specification,
                PageRequest.of(0, CONTEXT_REPORT_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"))
            )
            .stream()
            .map(report -> ChatbotReportContext.of(report, null, objectMapper))
            .toList();
    }

    private BigDecimal calculateDistanceMeters(
        BigDecimal sourceLatitude,
        BigDecimal sourceLongitude,
        BigDecimal targetLatitude,
        BigDecimal targetLongitude) {
        double sourceLatRadians = Math.toRadians(sourceLatitude.doubleValue());
        double sourceLngRadians = Math.toRadians(sourceLongitude.doubleValue());
        double targetLatRadians = Math.toRadians(targetLatitude.doubleValue());
        double targetLngRadians = Math.toRadians(targetLongitude.doubleValue());
        double cosineDistance = Math.sin(sourceLatRadians) * Math.sin(targetLatRadians)
            + Math.cos(sourceLatRadians) * Math.cos(targetLatRadians)
            * Math.cos(targetLngRadians - sourceLngRadians);
        double clampedCosineDistance = Math.max(-1.0, Math.min(1.0, cosineDistance));
        double distanceMeters = 6371000.0 * Math.acos(clampedCosineDistance);

        return BigDecimal.valueOf(distanceMeters).setScale(1, RoundingMode.HALF_UP);
    }
}
