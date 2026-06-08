package com.ssaika.ssiren.domain.agency.service;

import com.ssaika.ssiren.domain.agency.dto.response.AgencyTypeResponse;
import com.ssaika.ssiren.domain.agency.repository.AgencyTypeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgencyService {

    private final AgencyTypeRepository agencyTypeRepository;

    public List<AgencyTypeResponse> getAgencyTypes() {
        log.info("Get agency types.");

        return agencyTypeRepository.findAllByOrderByIdAsc()
                .stream()
                .map(AgencyTypeResponse::from)
                .toList();
    }
}