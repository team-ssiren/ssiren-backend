package com.ssaika.ssiren.domain.report.address;

import java.math.BigDecimal;

public interface AddressResolver {

    AddressSnapshot resolve(BigDecimal latitude, BigDecimal longitude);
}
