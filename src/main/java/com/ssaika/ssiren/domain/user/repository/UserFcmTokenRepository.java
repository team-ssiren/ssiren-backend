package com.ssaika.ssiren.domain.user.repository;

import com.ssaika.ssiren.domain.user.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {
}
