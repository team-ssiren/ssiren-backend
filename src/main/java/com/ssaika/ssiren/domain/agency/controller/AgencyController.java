package com.ssaika.ssiren.domain.agency.controller;

import com.ssaika.ssiren.domain.agency.dto.response.AgencyTypeResponse;
import com.ssaika.ssiren.domain.agency.service.AgencyService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import com.ssaika.ssiren.global.dto.ListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/agencies")
@Validated
public class AgencyController {

    private final AgencyService agencyService;

    @GetMapping
    public ResponseEntity<BaseResponse<ListResponseDto<AgencyTypeResponse>>> getAgencyTypes() {
        ListResponseDto<AgencyTypeResponse> response = ListResponseDto.from(
                agencyService.getAgencyTypes()
        );

        return ResponseEntity.ok(BaseResponse.success("처리 기관 타입 목록 조회 성공", response));
    }
}