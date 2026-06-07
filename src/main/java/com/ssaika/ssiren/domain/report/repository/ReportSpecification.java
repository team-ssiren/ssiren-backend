package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.global.enums.ReportStatus;
import java.time.LocalDateTime;
import org.springframework.data.jpa.domain.Specification;

public class ReportSpecification {

    private ReportSpecification() {
    }

    public static Specification<Report> belongsToUser(Long userId) {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<Report> hasStatus(ReportStatus status) {
        return (root, query, criteriaBuilder) -> status == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Report> hasCategory(Long categoryId) {
        return (root, query, criteriaBuilder) -> categoryId == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.equal(root.get("category").get("id"), categoryId);
    }

    public static Specification<Report> createdAtFrom(LocalDateTime from) {
        return (root, query, criteriaBuilder) -> from == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<Report> createdAtTo(LocalDateTime to) {
        return (root, query, criteriaBuilder) -> to == null
            ? criteriaBuilder.conjunction()
            : criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), to);
    }
}
