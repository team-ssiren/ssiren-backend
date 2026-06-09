package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.ReportCategoryMergeRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCategoryMergeRuleRepository extends JpaRepository<ReportCategoryMergeRule, Long> {

    @EntityGraph(attributePaths = {"category"})
    Optional<ReportCategoryMergeRule> findByCategory_Id(Long categoryId);
}
