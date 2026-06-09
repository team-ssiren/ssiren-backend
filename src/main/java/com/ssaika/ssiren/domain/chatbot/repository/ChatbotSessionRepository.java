package com.ssaika.ssiren.domain.chatbot.repository;

import com.ssaika.ssiren.domain.chatbot.entity.ChatbotSession;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotSessionRepository extends JpaRepository<ChatbotSession, Long> {

    Page<ChatbotSession> findAllByUser_Id(Long userId, Pageable pageable);

    Optional<ChatbotSession> findByIdAndUser_Id(Long id, Long userId);
}
