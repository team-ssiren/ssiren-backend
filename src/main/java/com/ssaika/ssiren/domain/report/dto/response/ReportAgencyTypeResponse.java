package com.ssaika.ssiren.domain.report.dto.response;

import com.ssaika.ssiren.domain.agency.entity.AgencyType;

public record ReportAgencyTypeResponse(
    Long id,
    String name
) {

    public static ReportAgencyTypeResponse from(AgencyType agencyType) {
        return new ReportAgencyTypeResponse(
            agencyType.getId(),
            agencyType.getName()
        );
    }
}
