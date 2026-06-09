package com.ssaika.ssiren.domain.report.client;

import com.ssaika.ssiren.domain.report.client.dto.request.ReportAiAnalyzeRequest;
import com.ssaika.ssiren.domain.report.client.dto.response.ReportAiAnalyzeResponse;

public interface ReportAiClient {

    ReportAiAnalyzeResponse analyzeReport(ReportAiAnalyzeRequest request);
}
