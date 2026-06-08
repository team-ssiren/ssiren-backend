package com.ssaika.ssiren.domain.report.dto.response;

public record ReportDraftCreateResponse(
    ReportDraftResponse reportDraft,
    ReportCategoryResponse category,
    ReportCategoryResponse parentCategory,
    ReportDepartmentResponse department,
    ReportAgencyTypeResponse agencyType,
    ReportAiAnalysisResponse analysis
) {
}
