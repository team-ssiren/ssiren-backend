package com.ssaika.ssiren.domain.report.dto.response;

import com.ssaika.ssiren.domain.agency.entity.Department;

public record ReportStatusHistoryDepartmentResponse(
        Long id,
        String name,
        ReportAgencyTypeResponse agencyType
) {

    public static ReportStatusHistoryDepartmentResponse from(Department department) {
        if (department == null) {
            return null;
        }

        return new ReportStatusHistoryDepartmentResponse(
                department.getId(),
                department.getName(),
                ReportAgencyTypeResponse.from(department.getAgencyType())
        );
    }
}
