package com.ssaika.ssiren.domain.chatbot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.chatbot.client.ChatbotAiClient;
import com.ssaika.ssiren.domain.chatbot.dto.request.ChatbotMessageSendRequest;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotMessageCursorResponse;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotSessionResponse;
import com.ssaika.ssiren.domain.chatbot.entity.ChatbotMessage;
import com.ssaika.ssiren.domain.chatbot.entity.ChatbotSession;
import com.ssaika.ssiren.domain.chatbot.repository.ChatbotMessageRepository;
import com.ssaika.ssiren.domain.chatbot.repository.ChatbotSessionRepository;
import com.ssaika.ssiren.domain.report.repository.ReportCategoryRepository;
import com.ssaika.ssiren.domain.report.repository.ReportRepository;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.enums.ChatbotSenderType;
import com.ssaika.ssiren.global.exception.CustomException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private ChatbotAiClient chatbotAiClient;

    @Mock
    private ChatbotMessageRepository chatbotMessageRepository;

    @Mock
    private ChatbotSessionRepository chatbotSessionRepository;

    @Mock
    private ReportCategoryRepository reportCategoryRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private User user;

    @Mock
    private ChatbotSession chatbotSession;

    @Mock
    private ChatbotMessage firstMessage;

    @Mock
    private ChatbotMessage secondMessage;

    @Mock
    private ChatbotMessage thirdMessage;

    private ChatbotService chatbotService;

    @BeforeEach
    void setUp() {
        chatbotService = new ChatbotService(chatbotAiClient, chatbotMessageRepository,
            chatbotSessionRepository, reportCategoryRepository, reportRepository, userRepository,
            new ObjectMapper());
    }

    @Test
    void getMyChatbotSessionsReturnsSessions() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 9, 12, 0);
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));
        when(chatbotSessionRepository.findAllByUser_Id(1L, pageable))
            .thenReturn(new PageImpl<>(List.of(chatbotSession), pageable, 1));
        when(chatbotSession.getId()).thenReturn(1L);
        when(chatbotSession.getTitle()).thenReturn("신고 카테고리 분류 상담");
        when(chatbotSession.getCreatedAt()).thenReturn(createdAt);

        Page<ChatbotSessionResponse> responses = chatbotService.getMyChatbotSessions(1L, pageable);

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).id()).isEqualTo(1L);
        assertThat(responses.getContent().get(0).title()).isEqualTo("신고 카테고리 분류 상담");
        verify(chatbotSessionRepository).findAllByUser_Id(1L, pageable);
    }

    @Test
    void getMyChatbotSessionsThrowsExceptionWhenUserDoesNotExist() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findById(1L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatbotService.getMyChatbotSessions(1L, pageable))
            .isInstanceOf(CustomException.class);

        verify(chatbotSessionRepository, never()).findAllByUser_Id(1L, pageable);
    }

    @Test
    void getChatbotMessagesReturnsCursorResponse() {
        Pageable pageable = PageRequest.of(0, 3);
        LocalDateTime createdAt = LocalDateTime.of(2026, 6, 9, 12, 35);
        when(chatbotSessionRepository.findByIdAndUser_Id(1L, 1L))
            .thenReturn(Optional.of(chatbotSession));
        when(chatbotMessageRepository.findAllBySession_IdOrderByIdDesc(1L, pageable))
            .thenReturn(List.of(firstMessage, secondMessage, thirdMessage));
        when(firstMessage.getId()).thenReturn(130L);
        when(firstMessage.getSenderType()).thenReturn(ChatbotSenderType.BOT);
        when(firstMessage.getMessage()).thenReturn("답변 메시지");
        when(firstMessage.getCreatedAt()).thenReturn(createdAt);
        when(secondMessage.getId()).thenReturn(129L);
        when(secondMessage.getSenderType()).thenReturn(ChatbotSenderType.USER);
        when(secondMessage.getMessage()).thenReturn("질문 메시지");
        when(secondMessage.getCreatedAt()).thenReturn(createdAt.minusSeconds(10));

        ChatbotMessageCursorResponse response = chatbotService.getChatbotMessages(1L, 1L, null, 2);

        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).id()).isEqualTo(130L);
        assertThat(response.nextCursor()).isEqualTo(129L);
        assertThat(response.hasNext()).isTrue();
        verify(chatbotMessageRepository).findAllBySession_IdOrderByIdDesc(1L, pageable);
    }

    @Test
    void getChatbotMessagesThrowsExceptionWhenSessionDoesNotExist() {
        when(chatbotSessionRepository.findByIdAndUser_Id(1L, 1L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatbotService.getChatbotMessages(1L, 1L, null, 20))
            .isInstanceOf(CustomException.class);

        verify(chatbotMessageRepository, never()).findAllBySession_IdOrderByIdDesc(1L,
            PageRequest.of(0, 21));
    }

    @Test
    void saveChatbotMessageDoesNotSaveMessagesWhenAiResponseFails() {
        ChatbotMessageSendRequest request = new ChatbotMessageSendRequest(
            "이 근처에 위험한 제보 있어?",
            new BigDecimal("36.36"),
            new BigDecimal("127.34")
        );
        when(chatbotSessionRepository.findById(1L))
            .thenReturn(Optional.of(chatbotSession));
        when(chatbotSession.getUser()).thenReturn(user);
        when(user.getId()).thenReturn(1L);
        when(chatbotMessageRepository.countBySession_Id(1L))
            .thenReturn(0L);
        when(chatbotMessageRepository.findAllBySession_IdOrderByIdDesc(1L, PageRequest.of(0, 10)))
            .thenReturn(List.of());
        when(chatbotAiClient.requestPlan(ArgumentMatchers.any()))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> chatbotService.saveChatbotMessage(1L, 1L, request))
            .isInstanceOf(CustomException.class);

        verify(chatbotMessageRepository).findAllBySession_IdOrderByIdDesc(1L, PageRequest.of(0, 10));
        verify(chatbotMessageRepository, never()).save(ArgumentMatchers.any());
    }
}
