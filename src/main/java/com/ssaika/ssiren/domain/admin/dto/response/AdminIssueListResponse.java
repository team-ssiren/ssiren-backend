package com.ssaika.ssiren.domain.admin.dto.response;

import java.util.List;

public record AdminIssueListResponse(
        List<AdminIssueResponse> issues
) {

    public static AdminIssueListResponse from(List<AdminIssueResponse> issues) {
        return new AdminIssueListResponse(issues);
    }
}