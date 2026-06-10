package com.ssaika.ssiren.domain.admin.dto.projection;

import java.math.BigDecimal;

public interface AdminDashboardIssueGroupPointProjection {

    Long getIssueGroupId();

    BigDecimal getGroupLatitude();

    BigDecimal getGroupLongitude();
}