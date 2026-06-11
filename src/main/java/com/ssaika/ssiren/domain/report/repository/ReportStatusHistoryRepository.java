package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ReportStatusHistoryRepository extends JpaRepository<ReportStatusHistory, Long> {

    @EntityGraph(attributePaths = {"report", "user", "department"})
    List<ReportStatusHistory> findByReport_IdOrderByCreatedAtAsc(Long reportId);

    @EntityGraph(attributePaths = {"report", "user", "department"})
    List<ReportStatusHistory> findByReport_IdInOrderByReport_IdAscCreatedAtAsc(Collection<Long> reportIds);
}
