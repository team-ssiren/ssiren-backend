package com.ssaika.ssiren.domain.report.dto.response;

import java.util.List;

public record IssueListResponse(
    List<IssueResponse> issues
) {

    public static IssueListResponse from(List<IssueResponse> issues) {
        return new IssueListResponse(issues);
    }
}
