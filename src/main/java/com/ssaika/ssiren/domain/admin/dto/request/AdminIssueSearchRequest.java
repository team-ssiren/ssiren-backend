package com.ssaika.ssiren.domain.admin.dto.request;

import com.ssaika.ssiren.global.enums.IssueGroupStatus;
import com.ssaika.ssiren.global.enums.ReportStatus;
import java.math.BigDecimal;

public record AdminIssueSearchRequest(
        String keyword,
        AdminIssueSortType sort,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng,
        Long categoryId,
        Long agencyTypeId,
        Long departmentId,
        Boolean myDepartmentOnly,
        Boolean deletedOnly,
        IssueGroupStatus status,
        ReportStatus reportStatus,
        BigDecimal riskMin,
        BigDecimal riskMax,
        String from,
        String to
) {

    public AdminIssueSortType resolvedSort() {
        return sort == null ? AdminIssueSortType.LATEST : sort;
    }
}