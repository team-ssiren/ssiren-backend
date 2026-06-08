package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.ReportCategory;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportCategoryRepository extends JpaRepository<ReportCategory, Long> {

    @EntityGraph(attributePaths = {"department", "parentCategory"})
    Optional<ReportCategory> findWithDepartmentById(Long id);

    @EntityGraph(attributePaths = {"parentCategory"})
    List<ReportCategory> findAllByOrderByIdAsc();
}
