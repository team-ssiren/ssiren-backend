package com.ssaika.ssiren.domain.chatbot.repository;

import com.ssaika.ssiren.domain.chatbot.entity.ChatbotMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {
}
