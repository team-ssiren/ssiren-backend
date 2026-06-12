package com.ssaika.ssiren.domain.agency.repository;

import com.ssaika.ssiren.domain.agency.entity.Department;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @EntityGraph(attributePaths = {"agencyType"})
    List<Department> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"agencyType"})
    List<Department> findByAgencyType_IdOrderByIdAsc(Long agencyTypeId);

    @EntityGraph(attributePaths = {"agencyType"})
    Optional<Department> findByAgencyType_NameAndName(String agencyTypeName, String name);
}
