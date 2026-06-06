package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.ReportStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportStatusHistoryRepository extends JpaRepository<ReportStatusHistory, Long> {
}
