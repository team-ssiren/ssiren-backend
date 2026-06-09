package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.domain.agency.entity.Department;

public record AdminReportDepartmentResponse(
        Long id,
        String name,
        AdminAgencyTypeResponse agencyType
) {

    public static AdminReportDepartmentResponse from(Department department) {
        return new AdminReportDepartmentResponse(
                department.getId(),
                department.getName(),
                AdminAgencyTypeResponse.from(department.getAgencyType())
        );
    }
}