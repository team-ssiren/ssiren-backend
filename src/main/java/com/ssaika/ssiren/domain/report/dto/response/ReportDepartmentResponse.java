package com.ssaika.ssiren.domain.report.dto.response;

import com.ssaika.ssiren.domain.agency.entity.Department;

public record ReportDepartmentResponse(
    Long id,
    String agencyName,
    String name,
    Long agencyId
) {

    public static ReportDepartmentResponse from(Department department) {
        return new ReportDepartmentResponse(
            department.getId(),
            department.getAgencyType().getName(),
            department.getName(),
            department.getAgencyType().getId()
        );
    }
}
