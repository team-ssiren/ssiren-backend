package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.domain.agency.entity.Department;

public record AdminTransferDepartmentResponse(
        Long id,
        String name,
        AdminTransferAgencyTypeResponse agencyType
) {

    public static AdminTransferDepartmentResponse from(Department department) {
        if (department == null) {
            return null;
        }

        return new AdminTransferDepartmentResponse(
                department.getId(),
                department.getName(),
                AdminTransferAgencyTypeResponse.from(department.getAgencyType())
        );
    }
}