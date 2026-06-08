package com.ssaika.ssiren.domain.agency.service;

import com.ssaika.ssiren.domain.agency.dto.response.DepartmentResponse;
import com.ssaika.ssiren.domain.agency.entity.Department;
import com.ssaika.ssiren.domain.agency.repository.DepartmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public List<DepartmentResponse> getDepartments(Long agencyTypeId) {
        log.info("Get departments. agencyTypeId={}", agencyTypeId);

        List<Department> departments = agencyTypeId == null
                ? departmentRepository.findAllByOrderByIdAsc()
                : departmentRepository.findByAgencyType_IdOrderByIdAsc(agencyTypeId);

        return departments.stream()
                .map(DepartmentResponse::from)
                .toList();
    }
}