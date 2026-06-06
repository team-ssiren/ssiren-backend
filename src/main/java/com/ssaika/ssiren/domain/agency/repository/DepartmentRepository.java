package com.ssaika.ssiren.domain.agency.repository;

import com.ssaika.ssiren.domain.agency.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
}
