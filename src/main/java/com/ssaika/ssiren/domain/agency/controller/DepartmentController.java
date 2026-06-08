package com.ssaika.ssiren.domain.agency.controller;

import com.ssaika.ssiren.domain.agency.dto.response.DepartmentResponse;
import com.ssaika.ssiren.domain.agency.service.DepartmentService;
import com.ssaika.ssiren.global.dto.BaseResponse;
import com.ssaika.ssiren.global.dto.ListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    @GetMapping
    public ResponseEntity<BaseResponse<ListResponseDto<DepartmentResponse>>> getDepartments(
            @RequestParam(required = false) Long agencyTypeId) {
        ListResponseDto<DepartmentResponse> response = ListResponseDto.from(
                departmentService.getDepartments(agencyTypeId)
        );

        return ResponseEntity.ok(BaseResponse.success("전체 부서 목록 조회 성공", response));
    }
}