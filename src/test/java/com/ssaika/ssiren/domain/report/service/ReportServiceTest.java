package com.ssaika.ssiren.domain.report.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssaika.ssiren.domain.report.entity.Report;
import com.ssaika.ssiren.domain.report.repository.ReportImageRepository;
import com.ssaika.ssiren.domain.report.repository.ReportReactionLogRepository;
import com.ssaika.ssiren.domain.report.repository.ReportRepository;
import com.ssaika.ssiren.domain.report.repository.ReportStatusHistoryRepository;
import com.ssaika.ssiren.global.exception.CustomException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ReportImageRepository reportImageRepository;

    @Mock
    private ReportStatusHistoryRepository reportStatusHistoryRepository;

    @Mock
    private ReportReactionLogRepository reportReactionLogRepository;

    private ReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportService(
            reportRepository,
            reportImageRepository,
            reportStatusHistoryRepository,
            reportReactionLogRepository,
            new ObjectMapper()
        );
    }

    @Test
    void getMyReportsAcceptsDateOnlyFilter() {
        Pageable pageable = PageRequest.of(0, 10);
        when(reportRepository.findAll(anyReportSpecification(), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        reportService.getMyReports(1L, null, null, "2026-06-01", "2026-06-30", pageable);

        verify(reportRepository).findAll(anyReportSpecification(), any(Pageable.class));
        verify(reportImageRepository, never()).findByReport_IdInOrderByReport_IdAscSortOrderAsc(any());
    }

    @Test
    void getMyReportsThrowsExceptionWhenFromIsAfterTo() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() ->
            reportService.getMyReports(1L, null, null, "2026-06-30", "2026-06-01", pageable))
            .isInstanceOf(CustomException.class)
            .hasMessage("조회 시작일은 종료일보다 늦을 수 없습니다.");

        verify(reportRepository, never()).findAll(anyReportSpecification(), any(Pageable.class));
    }

    @Test
    void getMyReportsThrowsExceptionWhenDateFormatIsInvalid() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() ->
            reportService.getMyReports(1L, null, null, "2026/06/01", null, pageable))
            .isInstanceOf(CustomException.class)
            .hasMessage("날짜 형식이 올바르지 않습니다.");

        verify(reportRepository, never()).findAll(anyReportSpecification(), any(Pageable.class));
    }

    @Test
    void getMyReportThrowsExceptionWhenReportDoesNotExist() {
        when(reportRepository.findByIdAndUser_Id(1L, 1L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getMyReport(1L, 1L))
            .isInstanceOf(CustomException.class)
            .hasMessage("제보를 찾을 수 없습니다.");

        verify(reportImageRepository, never()).findByReport_IdOrderBySortOrderAsc(any());
        verify(reportStatusHistoryRepository, never()).findByReport_IdOrderByCreatedAtAsc(any());
        verify(reportReactionLogRepository, never()).findByReport_IdOrderByCreatedAtAsc(any());
    }

    private Specification<Report> anyReportSpecification() {
        return any();
    }
}
