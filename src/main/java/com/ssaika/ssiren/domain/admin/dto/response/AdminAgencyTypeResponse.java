package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.domain.agency.entity.AgencyType;

public record AdminAgencyTypeResponse(
        Long id,
        String name
) {

    public static AdminAgencyTypeResponse from(AgencyType agencyType) {
        return new AdminAgencyTypeResponse(
                agencyType.getId(),
                agencyType.getName()
        );
    }
}