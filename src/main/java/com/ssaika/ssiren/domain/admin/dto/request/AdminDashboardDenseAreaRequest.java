package com.ssaika.ssiren.domain.admin.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AdminDashboardDenseAreaRequest(
        @NotNull
        BigDecimal latitude,

        @NotNull
        BigDecimal longitude,

        @NotNull
        @Positive
        Integer radiusMeters,

        @Positive
        Integer gridSizeMeters,

        @Positive
        Integer minIssueGroupCount,

        Boolean myDepartmentOnly
) {

    private static final int DEFAULT_GRID_SIZE_METERS = 300;
    private static final int DEFAULT_MIN_ISSUE_GROUP_COUNT = 2;

    public int resolvedGridSizeMeters() {
        return gridSizeMeters == null ? DEFAULT_GRID_SIZE_METERS : gridSizeMeters;
    }

    public int resolvedMinIssueGroupCount() {
        return minIssueGroupCount == null ? DEFAULT_MIN_ISSUE_GROUP_COUNT : minIssueGroupCount;
    }
}