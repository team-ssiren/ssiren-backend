package com.ssaika.ssiren.domain.agency.dto.response;

import com.ssaika.ssiren.domain.agency.entity.Department;

public record DepartmentResponse(
        Long id,
        String name,
        Long agencyTypeId,
        String agencyTypeName
) {

    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getAgencyType().getId(),
                department.getAgencyType().getName()
        );
    }
}