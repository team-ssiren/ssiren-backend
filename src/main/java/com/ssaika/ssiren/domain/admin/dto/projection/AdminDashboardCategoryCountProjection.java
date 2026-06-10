package com.ssaika.ssiren.domain.admin.dto.projection;

public interface AdminDashboardCategoryCountProjection {

    Long getCategoryId();

    String getCategoryCode();

    String getCategoryName();

    Long getReportCount();
}