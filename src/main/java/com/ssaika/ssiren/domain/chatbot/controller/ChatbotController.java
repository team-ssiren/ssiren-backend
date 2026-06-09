package com.ssaika.ssiren.domain.chatbot.controller;

import com.ssaika.ssiren.domain.chatbot.dto.request.ChatbotMessageSendRequest;
import com.ssaika.ssiren.domain.chatbot.dto.request.ChatbotSessionTitleUpdateRequest;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotMessageCursorResponse;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotMessageSendResponse;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotSessionResponse;
import com.ssaika.ssiren.domain.chatbot.dto.response.ChatbotSessionTitleUpdateResponse;
import com.ssaika.ssiren.domain.chatbot.service.ChatbotService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import com.ssaika.ssiren.global.dto.PageResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatbots")
@Validated
public class ChatbotController {

    private static final Long TEST_USER_ID = 1L;

    private final ChatbotService chatbotService;

    @GetMapping("/sessions")
    public ResponseEntity<BaseResponse<PageResponseDto<ChatbotSessionResponse>>> getMyChatbotSessions(
        @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
        Pageable pageable) {
        PageResponseDto<ChatbotSessionResponse> response = PageResponseDto.from(
            chatbotService.getMyChatbotSessions(TEST_USER_ID, pageable)
        );

        return ResponseEntity.ok(BaseResponse.success("내 챗봇 세션 목록 조회 성공", response));
    }

    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<BaseResponse<ChatbotMessageCursorResponse>> getChatbotMessages(
        @PathVariable Long sessionId,
        @RequestParam(required = false) Long cursor,
        @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size) {
        ChatbotMessageCursorResponse response = chatbotService.getChatbotMessages(TEST_USER_ID,
            sessionId, cursor, size);

        return ResponseEntity.ok(BaseResponse.success("채팅 내역 조회 성공", response));
    }

    @PostMapping("/sessions")
    public ResponseEntity<BaseResponse<ChatbotSessionResponse>> saveChatbotSession() {
        ChatbotSessionResponse response = chatbotService.saveChatbotSession(TEST_USER_ID);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.success(HttpStatus.CREATED, "챗봇 세션 생성 성공", response));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<BaseResponse<Void>> deleteChatbotSession(@PathVariable Long sessionId) {
        chatbotService.deleteChatbotSession(TEST_USER_ID, sessionId);

        return ResponseEntity.ok(BaseResponse.success("챗봇 세션 삭제 성공", null));
    }

    @PatchMapping("/sessions/{sessionId}")
    public ResponseEntity<BaseResponse<ChatbotSessionTitleUpdateResponse>> updateChatbotSessionTitle(
        @PathVariable Long sessionId,
        @Valid @RequestBody ChatbotSessionTitleUpdateRequest request) {
        ChatbotSessionTitleUpdateResponse response = chatbotService.updateChatbotSessionTitle(
            TEST_USER_ID,
            sessionId,
            request
        );

        return ResponseEntity.ok(BaseResponse.success("챗봇 제목 변경 성공", response));
    }

    @PostMapping("/sessions/{sessionId}")
    public ResponseEntity<BaseResponse<ChatbotMessageSendResponse>> saveChatbotMessage(
        @PathVariable Long sessionId,
        @Valid @RequestBody ChatbotMessageSendRequest request) {
        ChatbotMessageSendResponse response = chatbotService.saveChatbotMessage(TEST_USER_ID,
            sessionId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(BaseResponse.success(HttpStatus.CREATED, "챗봇 메시지 전송 성공", response));
    }
}
