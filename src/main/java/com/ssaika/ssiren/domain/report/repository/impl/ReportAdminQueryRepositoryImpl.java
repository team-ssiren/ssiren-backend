package com.ssaika.ssiren.domain.report.repository.impl;

import com.ssaika.ssiren.domain.admin.dto.request.AdminIssueSortType;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.repository.ReportAdminQueryRepository;
import com.ssaika.ssiren.global.enums.ReportStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ReportAdminQueryRepositoryImpl implements ReportAdminQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Report> findAdminIssueRepresentatives(
            Specification<Report> specification,
            AdminIssueSortType sortType
    ) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Report> query = criteriaBuilder.createQuery(Report.class);
        Root<Report> root = query.from(Report.class);

        fetchAdminIssueGraph(root);

        query.select(root);

        Predicate predicate = specification == null
                ? criteriaBuilder.conjunction()
                : specification.toPredicate(root, query, criteriaBuilder);

        query.where(predicate);
        query.orderBy(resolveOrders(criteriaBuilder, root, sortType));

        TypedQuery<Report> typedQuery = entityManager.createQuery(query);
        return typedQuery.getResultList();
    }

    private void fetchAdminIssueGraph(Root<Report> root) {
        root.fetch("user", JoinType.LEFT);
        root.fetch("category", JoinType.LEFT).fetch("parentCategory", JoinType.LEFT);
        root.fetch("issueGroup", JoinType.LEFT);
        root.fetch("department", JoinType.LEFT).fetch("agencyType", JoinType.LEFT);
    }

    private List<Order> resolveOrders(
            CriteriaBuilder criteriaBuilder,
            Root<Report> root,
            AdminIssueSortType sortType
    ) {
        if (sortType == AdminIssueSortType.RISK_DESC) {
            List<Order> orders = new ArrayList<>();
            orders.add(criteriaBuilder.asc(terminalStatusLastExpression(criteriaBuilder, root)));
            orders.add(criteriaBuilder.desc(root.get("issueGroup").get("riskScore")));
            orders.add(criteriaBuilder.desc(root.get("issueGroup").get("recentReportedAt")));
            orders.add(criteriaBuilder.desc(root.get("id")));
            return orders;
        }

        List<Order> orders = new ArrayList<>();
        orders.add(criteriaBuilder.asc(terminalStatusLastExpression(criteriaBuilder, root)));
        orders.add(criteriaBuilder.desc(root.get("issueGroup").get("recentReportedAt")));
        orders.add(criteriaBuilder.desc(root.get("id")));
        return orders;
    }

    private Expression<Integer> terminalStatusLastExpression(
            CriteriaBuilder criteriaBuilder,
            Root<Report> root
    ) {
        return criteriaBuilder.<Integer>selectCase()
                .when(root.get("status").in(ReportStatus.COMPLETED, ReportStatus.REJECTED), 1)
                .otherwise(0);
    }
}