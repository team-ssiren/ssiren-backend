package com.ssaika.ssiren.domain.chatbot.repository;

import com.ssaika.ssiren.domain.chatbot.entity.ChatbotSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, Long> {
}
