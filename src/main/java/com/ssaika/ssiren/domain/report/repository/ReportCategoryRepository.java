package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.ReportCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCategoryRepository extends JpaRepository<ReportCategory, Long> {
}
