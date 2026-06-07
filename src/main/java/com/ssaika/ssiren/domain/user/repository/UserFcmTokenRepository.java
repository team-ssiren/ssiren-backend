package com.ssaika.ssiren.domain.user.repository;

import com.ssaika.ssiren.domain.user.entity.UserFcmToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    Optional<UserFcmToken> findByFcmToken(String fcmToken);

    Optional<UserFcmToken> findByFcmTokenAndUserId(String fcmToken, Long userId);

    List<UserFcmToken> findAllByUserIdAndIsActiveTrue(Long userId);
}
