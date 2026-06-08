package com.ssaika.ssiren.domain.agency.dto.response;

import com.ssaika.ssiren.domain.agency.entity.AgencyType;

public record AgencyTypeResponse(
        Long id,
        String name
) {

    public static AgencyTypeResponse from(AgencyType agencyType) {
        return new AgencyTypeResponse(
                agencyType.getId(),
                agencyType.getName()
        );
    }
}