package com.ssaika.ssiren.domain.user.repository;

import com.ssaika.ssiren.domain.user.entity.OfficerDepartment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfficerDepartmentRepository extends JpaRepository<OfficerDepartment, Long> {

    @EntityGraph(attributePaths = {"department", "department.agencyType"})
    List<OfficerDepartment> findByUserId(Long userId);
}