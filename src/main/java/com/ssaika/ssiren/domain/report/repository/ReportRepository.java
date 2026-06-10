package com.ssaika.ssiren.domain.report.repository;

import com.ssaika.ssiren.domain.report.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report>, ReportAdminQueryRepository {

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

    @Override
    @EntityGraph(attributePaths = {
        "user",
        "category",
        "category.parentCategory",
        "issueGroup",
        "department",
        "department.agencyType"
    })
    List<Report> findAll(Specification<Report> specification, Sort sort);

    @EntityGraph(attributePaths = {
        "user",
        "category",
        "category.parentCategory",
        "issueGroup",
        "department",
        "department.agencyType"
    })
    Optional<Report> findByIdAndUser_Id(Long id, Long userId);

    @EntityGraph(attributePaths = {"issueGroup"})
    List<Report> findByIssueGroup_IdInAndIsDeletedFalse(Collection<Long> issueGroupIds);

    @EntityGraph(attributePaths = {"issueGroup"})
    List<Report> findByIssueGroup_IdAndIsDeletedFalse(Long issueGroupId);

    @Query(
        value = """
            select
                r.id as "candidateReportId",
                r.issue_group_id as "candidateIssueGroupId",
                ST_Distance(
                    r.location,
                    ST_SetSRID(
                        ST_MakePoint(cast(:longitude as double precision), cast(:latitude as double precision)),
                        4326
                    )::geography
                ) as "distanceMeters",
                (1 - (r.embedding <=> cast(:embedding as vector))) as "embeddingSimilarity"
            from reports r
            where r.category_id = :categoryId
              and r.department_id = :departmentId
              and r.is_deleted = false
              and r.status in ('SUBMITTED', 'RECEIVED', 'CHECKING', 'IN_PROGRESS', 'TRANSFERRED')
              and r.location is not null
              and r.embedding is not null
              and ST_DWithin(
                    r.location,
                    ST_SetSRID(
                        ST_MakePoint(cast(:longitude as double precision), cast(:latitude as double precision)),
                        4326
                    )::geography,
                    :linkRadiusMeters
                  )
              and (1 - (r.embedding <=> cast(:embedding as vector))) >= :minEmbeddingSimilarity
            order by "embeddingSimilarity" desc, "distanceMeters" asc
            """,
        nativeQuery = true
    )
    List<DuplicateReportCandidate> findDuplicateCandidates(
        @Param("categoryId") Long categoryId,
        @Param("departmentId") Long departmentId,
        @Param("latitude") BigDecimal latitude,
        @Param("longitude") BigDecimal longitude,
        @Param("embedding") String embedding,
        @Param("linkRadiusMeters") Integer linkRadiusMeters,
        @Param("minEmbeddingSimilarity") BigDecimal minEmbeddingSimilarity
    );

    @Query("""
    select r
    from Report r
    join fetch r.user
    join fetch r.issueGroup
    join fetch r.department d
    join fetch d.agencyType
    where r.issueGroup.id = :issueGroupId
      and r.isDeleted = false
    """)
    List<Report> findReportsForAdminStatusUpdate(@Param("issueGroupId") Long issueGroupId);

    @EntityGraph(attributePaths = {
            "user",
            "category",
            "category.parentCategory",
            "issueGroup",
            "department",
            "department.agencyType"
    })
    List<Report> findByIssueGroup_IdIn(Collection<Long> issueGroupIds);
}
