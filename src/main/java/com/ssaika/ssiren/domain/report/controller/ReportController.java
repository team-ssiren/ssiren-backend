package com.ssaika.ssiren.domain.report.controller;

import com.ssaika.ssiren.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reports")
@Validated
public class ReportController {

    private final ReportService reportService;
}
