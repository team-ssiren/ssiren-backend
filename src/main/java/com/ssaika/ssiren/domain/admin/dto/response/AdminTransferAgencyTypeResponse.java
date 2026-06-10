package com.ssaika.ssiren.domain.admin.dto.response;

import com.ssaika.ssiren.domain.agency.entity.AgencyType;

public record AdminTransferAgencyTypeResponse(
        Long id,
        String name
) {

    public static AdminTransferAgencyTypeResponse from(AgencyType agencyType) {
        if (agencyType == null) {
            return null;
        }

        return new AdminTransferAgencyTypeResponse(
                agencyType.getId(),
                agencyType.getName()
        );
    }
}