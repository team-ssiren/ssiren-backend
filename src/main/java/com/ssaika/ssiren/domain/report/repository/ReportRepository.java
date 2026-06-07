package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {

    @Override
    @EntityGraph(attributePaths = {
        "user",
        "category",
        "category.parentCategory",
        "issueGroup",
        "department",
        "department.agencyType"
    })
    Page<Report> findAll(Specification<Report> specification, Pageable pageable);
}
