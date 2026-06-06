package com.ssaika.ssiren.domain.agency.controller;

import com.ssaika.ssiren.domain.agency.service.AgencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/agencies")
@Validated
public class AgencyController {

    private final AgencyService agencyService;
}
