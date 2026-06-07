package com.ssaika.ssiren.domain.user.repository;

import com.ssaika.ssiren.domain.user.entity.UserConsent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {

    Optional<UserConsent> findByUserId(Long userId);
}
