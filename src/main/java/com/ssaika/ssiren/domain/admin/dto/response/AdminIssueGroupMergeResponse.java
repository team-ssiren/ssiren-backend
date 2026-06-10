package com.ssaika.ssiren.domain.admin.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record AdminIssueGroupMergeResponse(
        Long targetIssueGroupId,
        List<Long> mergedIssueGroupIds,
        Integer movedReportCount,
        List<Long> movedReportIds,
        Integer targetReportCount,
        BigDecimal groupDiameterMeters
) {

    public static AdminIssueGroupMergeResponse of(
            Long targetIssueGroupId,
            List<Long> mergedIssueGroupIds,
            List<Long> movedReportIds,
            Integer targetReportCount,
            BigDecimal groupDiameterMeters
    ) {
        return new AdminIssueGroupMergeResponse(
                targetIssueGroupId,
                mergedIssueGroupIds,
                movedReportIds.size(),
                movedReportIds,
                targetReportCount,
                groupDiameterMeters
        );
    }
}