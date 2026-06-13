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
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotTitleRequest;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotTitleResponse;
import com.ssaika.ssiren.domain.chatbot.client.dto.ChatbotUserLocation;
import com.ssaika.ssiren.domain.chatbot.dto.request.ChatbotMessageSendRequest;
import com.ssaika.ssiren.domain.chatbot.dto.request.ChatbotSessionTitleUpdateRequest;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotMessageCursorResponse;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotMessageResponse;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotMessageSendResponse;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotSessionResponse;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotSessionTitleUpdateResponse;
import com.ssaika.ssiren.domain.chatbot.entity.ChatbotMessage;
import com.ssaika.ssiren.domain.chatbot.entity.ChatbotSession;
import com.ssaika.ssiren.domain.chatbot.repository.ChatbotMessageRepository;
import com.ssaika.ssiren.domain.chatbot.repository.ChatbotSessionRepository;
import com.ssaika.ssiren.domain.report.entity.IssueGroup;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.entity.ReportCategory;
import com.ssaika.ssiren.domain.report.repository.ReportCategoryRepository;
import com.ssaika.ssiren.domain.report.repository.ReportRepository;
import com.ssaika.ssiren.domain.report.repository.ReportSpecification;
import com.ssaika.ssiren.domain.user.entity.User;
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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Slf4j
@Transactional(readOnly = true)
public class ChatbotService {

    private static final String NEW_CHAT_TITLE = "새 대화";
    private static final int HISTORY_SIZE = 10;

    private final ChatbotAiClient chatbotAiClient;
    private final ChatbotMessageRepository chatbotMessageRepository;
    private final ChatbotSessionRepository chatbotSessionRepository;
    private final ReportCategoryRepository reportCategoryRepository;
    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @Autowired
    public ChatbotService(
        ChatbotAiClient chatbotAiClient,
        ChatbotMessageRepository chatbotMessageRepository,
        ChatbotSessionRepository chatbotSessionRepository,
        ReportCategoryRepository reportCategoryRepository,
        ReportRepository reportRepository,
        UserRepository userRepository,
        ObjectMapper objectMapper,
        PlatformTransactionManager transactionManager) {
        this.chatbotAiClient = chatbotAiClient;
        this.chatbotMessageRepository = chatbotMessageRepository;
        this.chatbotSessionRepository = chatbotSessionRepository;
        this.reportCategoryRepository = reportCategoryRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.transactionManager = transactionManager;
    }

    public ChatbotService(
        ChatbotAiClient chatbotAiClient,
        ChatbotMessageRepository chatbotMessageRepository,
        ChatbotSessionRepository chatbotSessionRepository,
        ReportCategoryRepository reportCategoryRepository,
        ReportRepository reportRepository,
        UserRepository userRepository,
        ObjectMapper objectMapper) {
        this.chatbotAiClient = chatbotAiClient;
        this.chatbotMessageRepository = chatbotMessageRepository;
        this.chatbotSessionRepository = chatbotSessionRepository;
        this.reportCategoryRepository = reportCategoryRepository;
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.transactionManager = null;
    }

    public Page<ChatbotSessionResponse> getMyChatbotSessions(Long userId, Pageable pageable) {
        userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                ErrorCode.USER_NOT_FOUND));

        return chatbotSessionRepository.findAllByUser_Id(userId, pageable)
            .map(ChatbotSessionResponse::from);
    }

    @Transactional
    public ChatbotSessionResponse saveChatbotSession(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                ErrorCode.USER_NOT_FOUND));
        ChatbotSession session = chatbotSessionRepository.save(ChatbotSession.create(
            user,
            NEW_CHAT_TITLE,
            LocalDateTime.now()
        ));

        return ChatbotSessionResponse.from(session);
    }

    public ChatbotMessageCursorResponse getChatbotMessages(Long userId, Long sessionId, Long cursor,
        Integer size) {
        getOwnedSession(userId, sessionId);

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
    public void deleteChatbotSession(Long userId, Long sessionId) {
        ChatbotSession session = getOwnedSession(userId, sessionId);

        chatbotSessionRepository.delete(session);
    }

    @Transactional
    public ChatbotSessionTitleUpdateResponse updateChatbotSessionTitle(
        Long userId,
        Long sessionId,
        ChatbotSessionTitleUpdateRequest request) {
        ChatbotSession session = getOwnedSession(userId, sessionId);
        session.updateTitle(request.title());

        return ChatbotSessionTitleUpdateResponse.from(session);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public ChatbotMessageSendResponse saveChatbotMessage(
        Long userId,
        Long sessionId,
        ChatbotMessageSendRequest request) {
        ChatbotMessagePreparation preparation = executeReadTransaction(() -> {
            getOwnedSession(userId, sessionId);
            List<ChatbotHistoryMessage> history = getRecentHistory(sessionId);

            return new ChatbotMessagePreparation(history);
        });
        ChatbotAnswerResult answerResult = resolveBotAnswer(userId, request, preparation.history())
            .orElseThrow(() -> new CustomException(
                ErrorCode.CHATBOT_AI_RESPONSE_FAILED.getMessage(),
                ErrorCode.CHATBOT_AI_RESPONSE_FAILED
            ));

        return executeWriteTransaction(() -> saveMessages(
            userId,
            sessionId,
            request,
            answerResult
        ));
    }

    private ChatbotMessageSendResponse saveMessages(
        Long userId,
        Long sessionId,
        ChatbotMessageSendRequest request,
        ChatbotAnswerResult answerResult) {
        ChatbotSession session = getOwnedSession(userId, sessionId);
        LocalDateTime now = LocalDateTime.now();
        boolean shouldUpdateTitle = NEW_CHAT_TITLE.equals(session.getTitle())
            && chatbotMessageRepository.countBySession_Id(sessionId) == 0;
        chatbotMessageRepository.save(ChatbotMessage.create(
            session,
            ChatbotSenderType.USER,
            request.message(),
            now
        ));
        chatbotMessageRepository.save(ChatbotMessage.create(
            session,
            ChatbotSenderType.BOT,
            answerResult.answer(),
            LocalDateTime.now()
        ));

        if (shouldUpdateTitle) {
            updateChatTitle(sessionId, session, request.message(), answerResult.answer());
        }

        return ChatbotMessageSendResponse.of(answerResult.answer(), answerResult.usedReportIds());
    }

    private void generateSessionTitle(ChatbotSession session, String question, String botAnswer) {
        chatbotAiClient.requestTitle(new ChatbotTitleRequest(question, botAnswer))
            .map(ChatbotTitleResponse::title)
            .map(String::strip)
            .filter(title -> !title.isBlank())
            .ifPresent(session::updateTitle);
    }

    private ChatbotSession getOwnedSession(Long userId, Long sessionId) {
        ChatbotSession session = chatbotSessionRepository.findById(sessionId)
            .orElseThrow(() -> new CustomException(
                ErrorCode.CHATBOT_SESSION_NOT_FOUND.getMessage(),
                ErrorCode.CHATBOT_SESSION_NOT_FOUND
            ));
        if (!session.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN.getMessage(), ErrorCode.FORBIDDEN);
        }

        return session;
    }

    private <T> T executeReadTransaction(Supplier<T> supplier) {
        if (transactionManager == null) {
            return supplier.get();
        }
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setReadOnly(true);

        return Objects.requireNonNull(transactionTemplate.execute(status -> supplier.get()));
    }

    private <T> T executeWriteTransaction(Supplier<T> supplier) {
        if (transactionManager == null) {
            return supplier.get();
        }
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        return Objects.requireNonNull(transactionTemplate.execute(status -> supplier.get()));
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

    private Optional<ChatbotAnswerResult> resolveBotAnswer(
        Long userId,
        ChatbotMessageSendRequest request,
        List<ChatbotHistoryMessage> history) {
        return chatbotAiClient.requestPlan(new ChatbotPlanRequest(request.message(), history))
            .flatMap(plan -> resolveBotAnswer(userId, request, history, plan));
    }

    private ChatbotTitleSource createTitleSource(
        Long sessionId,
        String currentQuestion,
        String currentAnswer) {
        String question = chatbotMessageRepository.findFirstBySession_IdAndSenderTypeOrderByIdAsc(
                sessionId,
                ChatbotSenderType.USER
            )
            .map(ChatbotMessage::getMessage)
            .orElse(currentQuestion);
        String answer = chatbotMessageRepository.findFirstBySession_IdAndSenderTypeOrderByIdAsc(
                sessionId,
                ChatbotSenderType.BOT
            )
            .map(ChatbotMessage::getMessage)
            .orElse(currentAnswer);

        return new ChatbotTitleSource(question, answer);
    }

    private void updateChatTitle(
        Long sessionId,
        ChatbotSession session,
        String currentQuestion,
        String currentAnswer) {
        ChatbotTitleSource titleSource = createTitleSource(sessionId, currentQuestion, currentAnswer);
        chatbotAiClient.requestTitle(new ChatbotTitleRequest(titleSource.question(), titleSource.answer()))
            .map(ChatbotTitleResponse::title)
            .map(String::strip)
            .filter(title -> !title.isBlank())
            .ifPresent(session::updateTitle);
    }

    private Optional<ChatbotAnswerResult> resolveBotAnswer(
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
                : Optional.of(new ChatbotAnswerResult(plan.answer(), null));
        }

        ChatbotAnswerContext context = createAnswerContext(userId, plan, request);
        ChatbotAnswerRequest answerRequest = new ChatbotAnswerRequest(
            request.message(),
            history,
            context
        );

        return chatbotAiClient.requestAnswer(answerRequest)
            .filter(answerResponse -> answerResponse.answer() != null
                && !answerResponse.answer().isBlank())
            .map(answerResponse -> new ChatbotAnswerResult(
                answerResponse.answer(),
                answerResponse.usedReportIds()
            ));
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
            case ANSWER_DIRECT -> throw new CustomException(
                ErrorCode.CHATBOT_AI_RESPONSE_FAILED.getMessage(),
                ErrorCode.CHATBOT_AI_RESPONSE_FAILED
            );
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
                Sort.by(Sort.Direction.DESC, "issueGroup.riskScore")
            )
            .stream()
            .map(report -> ChatbotReportContext.of(
                report,
                calculateDistanceMeters(request, report),
                objectMapper
            ))
            .toList();
    }

    private List<ChatbotReportContext> getMyReportContexts(Long userId) {
        Specification<Report> specification = ReportSpecification.belongsToUser(userId)
            .and(ReportSpecification.isNotDeleted());

        return reportRepository.findAll(
                specification,
                Sort.by(Sort.Direction.DESC, "createdAt")
            )
            .stream()
            .map(report -> ChatbotReportContext.of(report, null, objectMapper))
            .toList();
    }

    private BigDecimal calculateDistanceMeters(ChatbotMessageSendRequest request, Report report) {
        IssueGroup issueGroup = report.getIssueGroup();
        if (issueGroup == null
            || issueGroup.getGroupLatitude() == null
            || issueGroup.getGroupLongitude() == null) {
            log.error("Invalid report context for chatbot. reportId={}, issueGroupId={}",
                report.getId(), issueGroup == null ? null : issueGroup.getId());
            throw new CustomException(
                ErrorCode.CHATBOT_REPORT_CONTEXT_INVALID.getMessage(),
                ErrorCode.CHATBOT_REPORT_CONTEXT_INVALID
            );
        }

        return calculateDistanceMeters(
            request.latitude(),
            request.longitude(),
            issueGroup.getGroupLatitude(),
            issueGroup.getGroupLongitude()
        );
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

    private record ChatbotMessagePreparation(
        List<ChatbotHistoryMessage> history
    ) {
    }

    private record ChatbotAnswerResult(
        String answer,
        List<Long> usedReportIds
    ) {
    }

    private record ChatbotTitleSource(
        String question,
        String answer
    ) {
    }
}
