package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.ReportReactionLog;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportReactionLogRepository extends JpaRepository<ReportReactionLog, Long> {

    @EntityGraph(attributePaths = {"report", "user"})
    List<ReportReactionLog> findByReport_IdOrderByCreatedAtAsc(Long reportId);
}
