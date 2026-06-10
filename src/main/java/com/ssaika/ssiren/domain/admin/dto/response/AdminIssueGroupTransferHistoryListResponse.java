package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.domain.report.entity.IssueGroupTransferHistory;

import java.util.List;

public record AdminIssueGroupTransferHistoryListResponse(
        List<AdminIssueGroupTransferHistoryResponse> transferHistories
) {

    public static AdminIssueGroupTransferHistoryListResponse from(
            List<IssueGroupTransferHistory> histories
    ) {
        return new AdminIssueGroupTransferHistoryListResponse(
                histories.stream()
                        .map(history -> AdminIssueGroupTransferHistoryResponse.of(history, null))
                        .toList()
        );
    }
}