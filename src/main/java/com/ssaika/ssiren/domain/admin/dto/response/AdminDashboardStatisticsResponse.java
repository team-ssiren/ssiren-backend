package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.domain.admin.dto.projection.AdminDashboardStatisticsProjection;

public record AdminDashboardStatisticsResponse(
        Long totalReportCount,
        Long submittedReportCount,
        Long processingReportCount,
        Long completedReportCount,
        Long delayedReportCount,
        Long monthlyCompletedReportCount,
        Long todayNewReportCount
) {

    public static AdminDashboardStatisticsResponse from(AdminDashboardStatisticsProjection projection) {
        return new AdminDashboardStatisticsResponse(
                projection.getTotalReportCount(),
                projection.getSubmittedReportCount(),
                projection.getProcessingReportCount(),
                projection.getCompletedReportCount(),
                projection.getDelayedReportCount(),
                projection.getMonthlyCompletedReportCount(),
                projection.getTodayNewReportCount()
        );
    }
}