package com.ssaika.ssiren.domain.chatbot.controller;

import com.ssaika.ssiren.domain.chatbot.service.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/chatbot")
@Validated
public class ChatbotController {

    private final ChatbotService chatbotService;
}
