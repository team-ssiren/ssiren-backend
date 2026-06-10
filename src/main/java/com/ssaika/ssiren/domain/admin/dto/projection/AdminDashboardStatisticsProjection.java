package com.ssaika.ssiren.domain.admin.dto.projection;

public interface AdminDashboardStatisticsProjection {

    Long getTotalReportCount();

    Long getProcessingReportCount();

    Long getCompletedReportCount();

    Long getDelayedReportCount();

    Long getMonthlyCompletedReportCount();

    Long getTodayNewReportCount();
}