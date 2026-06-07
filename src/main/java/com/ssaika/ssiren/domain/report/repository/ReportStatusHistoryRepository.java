package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportStatusHistoryRepository extends JpaRepository<ReportStatusHistory, Long> {

    @EntityGraph(attributePaths = {"report", "user"})
    List<ReportStatusHistory> findByReport_IdOrderByCreatedAtAsc(Long reportId);
}
