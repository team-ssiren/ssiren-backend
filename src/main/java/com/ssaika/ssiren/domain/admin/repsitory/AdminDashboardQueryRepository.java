package com.ssaika.ssiren.domain.admin.repsitory;

import com.ssaika.ssiren.domain.admin.dto.projection.AdminDashboardCategoryCountProjection;
import com.ssaika.ssiren.domain.admin.dto.projection.AdminDashboardIssueGroupPointProjection;
import com.ssaika.ssiren.domain.admin.dto.projection.AdminDashboardStatisticsProjection;
import com.ssaika.ssiren.domain.report.entity.Report;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface AdminDashboardQueryRepository extends Repository<Report, Long> {

    @Query(value = """
        select
            coalesce(sum(coalesce(ig.report_count, 0) + coalesce(ig.yes_count, 0)), 0) as "totalReportCount",
            coalesce(sum(case
                when r.status = 'SUBMITTED' then coalesce(ig.report_count, 0) + coalesce(ig.yes_count, 0)
                else 0
            end), 0) as "submittedReportCount",
            coalesce(sum(case
                when r.status in ('CHECKING', 'IN_PROGRESS') then coalesce(ig.report_count, 0) + coalesce(ig.yes_count, 0)
                else 0
            end), 0) as "processingReportCount",
            coalesce(sum(case
                when r.status = 'COMPLETED' then coalesce(ig.report_count, 0) + coalesce(ig.yes_count, 0)
                else 0
            end), 0) as "completedReportCount",
            coalesce(sum(case
                when r.status in ('SUBMITTED', 'RECEIVED', 'CHECKING', 'IN_PROGRESS')
                 and r.created_at < :delayThreshold then coalesce(ig.report_count, 0) + coalesce(ig.yes_count, 0)
                else 0
            end), 0) as "delayedReportCount",
            (
                select coalesce(sum(coalesce(rhig.report_count, 0) + coalesce(rhig.yes_count, 0)), 0)
                from (
                    select distinct h.report_id
                    from report_status_histories h
                    where h.new_status = 'COMPLETED'
                      and h.created_at >= :monthStart
                      and h.created_at < :nextMonthStart
                ) completed_reports
                join reports rh on rh.id = completed_reports.report_id
                join issue_groups rhig on rhig.id = rh.issue_group_id
                join departments rhd on rhd.id = rh.department_id
                where rh.is_representative = true
                  and rh.is_deleted = false
                  and (
                      (:myDepartmentOnly = true and rh.department_id in (:departmentIds))
                      or
                      (:myDepartmentOnly = false and rhd.agency_type_id in (:agencyTypeIds))
                  )
            ) as "monthlyCompletedReportCount",
            coalesce(sum(case
                when r.created_at >= :todayStart
                 and r.created_at < :tomorrowStart then coalesce(ig.report_count, 0) + coalesce(ig.yes_count, 0)
                else 0
            end), 0) as "todayNewReportCount"
        from reports r
        join issue_groups ig on ig.id = r.issue_group_id
        join departments d on d.id = r.department_id
        where r.is_representative = true
          and r.is_deleted = false
          and (
              (:myDepartmentOnly = true and r.department_id in (:departmentIds))
              or
              (:myDepartmentOnly = false and d.agency_type_id in (:agencyTypeIds))
          )
    """, nativeQuery = true)
    AdminDashboardStatisticsProjection getStatistics(
            @Param("myDepartmentOnly") boolean myDepartmentOnly,
            @Param("departmentIds") List<Long> departmentIds,
            @Param("agencyTypeIds") List<Long> agencyTypeIds,
            @Param("delayThreshold") LocalDateTime delayThreshold,
            @Param("monthStart") LocalDateTime monthStart,
            @Param("nextMonthStart") LocalDateTime nextMonthStart,
            @Param("todayStart") LocalDateTime todayStart,
            @Param("tomorrowStart") LocalDateTime tomorrowStart
    );

    @Query(value = """
        select
            coalesce(parent.id, c.id) as "categoryId",
            coalesce(parent.category_code, c.category_code) as "categoryCode",
            coalesce(parent.category_name, c.category_name) as "categoryName",
            coalesce(sum(coalesce(ig.report_count, 0) + coalesce(ig.yes_count, 0)), 0) as "reportCount"
        from reports r
        join issue_groups ig on ig.id = r.issue_group_id
        join departments d on d.id = r.department_id
        join report_categories c on c.id = r.category_id
        left join report_categories parent on parent.id = c.parent_category_id
        where r.is_representative = true
          and r.is_deleted = false
          and (
              (:myDepartmentOnly = true and r.department_id in (:departmentIds))
              or
              (:myDepartmentOnly = false and d.agency_type_id in (:agencyTypeIds))
          )
        group by
            coalesce(parent.id, c.id),
            coalesce(parent.category_code, c.category_code),
            coalesce(parent.category_name, c.category_name)
        order by coalesce(sum(coalesce(ig.report_count, 0) + coalesce(ig.yes_count, 0)), 0) desc
        """, nativeQuery = true)
    List<AdminDashboardCategoryCountProjection> getCategoryStatistics(
            @Param("myDepartmentOnly") boolean myDepartmentOnly,
            @Param("departmentIds") List<Long> departmentIds,
            @Param("agencyTypeIds") List<Long> agencyTypeIds
    );

    @Query(value = """
        select distinct
            ig.id as "issueGroupId",
            ig.group_latitude as "groupLatitude",
            ig.group_longitude as "groupLongitude",
            coalesce(ig.report_count, 0) + coalesce(ig.yes_count, 0) as "reportCount"
        from issue_groups ig
        join reports r on r.issue_group_id = ig.id
        join departments d on d.id = r.department_id
        where r.is_representative = true
          and r.is_deleted = false
          and (
              (:myDepartmentOnly = true and r.department_id in (:departmentIds))
              or
              (:myDepartmentOnly = false and d.agency_type_id in (:agencyTypeIds))
          )
          and (
              6371000 * acos(
                  least(1, greatest(-1,
                      sin(radians(cast(:latitude as double precision)))
                      * sin(radians(cast(ig.group_latitude as double precision)))
                      + cos(radians(cast(:latitude as double precision)))
                      * cos(radians(cast(ig.group_latitude as double precision)))
                      * cos(
                          radians(cast(ig.group_longitude as double precision))
                          - radians(cast(:longitude as double precision))
                      )
                  ))
              )
          ) <= :radiusMeters
        """, nativeQuery = true)
    List<AdminDashboardIssueGroupPointProjection> findIssueGroupPointsForDenseArea(
            @Param("myDepartmentOnly") boolean myDepartmentOnly,
            @Param("departmentIds") List<Long> departmentIds,
            @Param("agencyTypeIds") List<Long> agencyTypeIds,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusMeters") Integer radiusMeters
    );
}
