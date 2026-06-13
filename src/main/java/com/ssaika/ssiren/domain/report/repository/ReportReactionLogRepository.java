package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.ReportReactionLog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportReactionLogRepository extends JpaRepository<ReportReactionLog, Long> {

    @EntityGraph(attributePaths = {"report", "user"})
    List<ReportReactionLog> findByReport_IdOrderByCreatedAtAsc(Long reportId);

    @EntityGraph(attributePaths = {"report", "user"})
    Optional<ReportReactionLog> findFirstByReport_IdAndUser_IdOrderByCreatedAtDesc(Long reportId, Long userId);
}
