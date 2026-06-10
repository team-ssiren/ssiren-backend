package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.ReportImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface ReportImageRepository extends JpaRepository<ReportImage, Long> {

    List<ReportImage> findByReport_IdInOrderByReport_IdAscSortOrderAsc(Collection<Long> reportIds);

    List<ReportImage> findByReport_IdOrderBySortOrderAsc(Long reportId);
}
