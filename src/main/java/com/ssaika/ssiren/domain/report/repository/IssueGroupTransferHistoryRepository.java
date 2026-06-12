package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.IssueGroupTransferHistory;
import com.ssaika.ssiren.domain.report.entity.IssueGroupTransferHistoryStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IssueGroupTransferHistoryRepository
        extends JpaRepository<IssueGroupTransferHistory, Long> {

    boolean existsByIssueGroup_IdAndStatus(
            Long issueGroupId,
            IssueGroupTransferHistoryStatus status
    );

    @EntityGraph(attributePaths = {
            "issueGroup",
            "fromDepartment",
            "fromDepartment.agencyType",
            "targetDepartment",
            "targetDepartment.agencyType",
            "requestUser",
            "responseUser"
    })
    Optional<IssueGroupTransferHistory> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {
            "issueGroup",
            "fromDepartment",
            "fromDepartment.agencyType",
            "targetDepartment",
            "targetDepartment.agencyType",
            "requestUser",
            "responseUser"
    })
    List<IssueGroupTransferHistory> findByTargetDepartment_IdInAndStatusOrderByCreatedAtDesc(
            Collection<Long> targetDepartmentIds,
            IssueGroupTransferHistoryStatus status
    );

    @EntityGraph(attributePaths = {
            "issueGroup",
            "fromDepartment",
            "fromDepartment.agencyType",
            "targetDepartment",
            "targetDepartment.agencyType",
            "requestUser",
            "responseUser"
    })
    List<IssueGroupTransferHistory> findByRequestUser_IdOrderByCreatedAtDesc(
            Long requestUserId
    );

    @EntityGraph(attributePaths = {
            "issueGroup",
            "fromDepartment",
            "fromDepartment.agencyType",
            "targetDepartment",
            "targetDepartment.agencyType",
            "requestUser",
            "responseUser"
    })
    List<IssueGroupTransferHistory> findByRequestUser_IdAndStatusOrderByCreatedAtDesc(
            Long requestUserId,
            IssueGroupTransferHistoryStatus status
    );
}