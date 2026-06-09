package com.ssaika.ssiren.domain.chatbot.repository;

import com.ssaika.ssiren.domain.chatbot.entity.ChatbotMessage;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {

    List<ChatbotMessage> findAllBySession_IdOrderByIdDesc(Long sessionId, Pageable pageable);

    List<ChatbotMessage> findAllBySession_IdAndIdLessThanOrderByIdDesc(Long sessionId, Long cursor,
        Pageable pageable);

    Long countBySession_Id(Long sessionId);
}
