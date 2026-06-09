package com.ssaika.ssiren.domain.agency.repository;

import com.ssaika.ssiren.domain.agency.entity.AgencyType;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ssaika.ssiren.domain.agency.entity.AgencyType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgencyTypeRepository extends JpaRepository<AgencyType, Long> {

    List<AgencyType> findAllByOrderByIdAsc();
}