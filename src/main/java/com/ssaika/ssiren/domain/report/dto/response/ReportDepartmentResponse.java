package com.ssaika.ssiren.domain.report.dto.response;

import com.ssaika.ssiren.domain.agency.entity.Department;

public record ReportDepartmentResponse(
    Long id,
    String name,
    Long agencyTypeId,
    String agencyTypeName
) {

    public static ReportDepartmentResponse from(Department department) {
        return new ReportDepartmentResponse(
            department.getId(),
            department.getName(),
            department.getAgencyType().getId(),
            department.getAgencyType().getName()
        );
    }
}
