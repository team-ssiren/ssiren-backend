package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueSortType;
import com.ssaika.ssiren.domain.report.entity.Report;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface ReportAdminQueryRepository {

    List<Report> findAdminIssueRepresentatives(
            Specification<Report> specification,
            AdminIssueSortType sortType
    );
}