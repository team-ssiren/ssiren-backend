package com.ssaika.ssiren.domain.report.repository;

import java.math.BigDecimal;

public interface DuplicateReportCandidate {

    Long getCandidateReportId();

    Long getCandidateIssueGroupId();

    BigDecimal getDistanceMeters();

    BigDecimal getEmbeddingSimilarity();
}
