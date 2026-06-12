package com.ssaika.ssiren.domain.chatbot.repository;

import com.ssaika.ssiren.domain.chatbot.entity.ChatbotMessage;
import com.ssaika.ssiren.global.enums.ChatbotSenderType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {

    long countBySession_Id(Long sessionId);

    List<ChatbotMessage> findAllBySession_IdOrderByIdDesc(Long sessionId, Pageable pageable);

    List<ChatbotMessage> findAllBySession_IdAndIdLessThanOrderByIdDesc(Long sessionId, Long cursor,
        Pageable pageable);

    Optional<ChatbotMessage> findFirstBySession_IdAndSenderTypeOrderByIdAsc(
        Long sessionId,
        ChatbotSenderType senderType);
}
