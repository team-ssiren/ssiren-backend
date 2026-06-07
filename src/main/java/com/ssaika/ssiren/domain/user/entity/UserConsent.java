package com.ssaika.ssiren.domain.user.entity;

import com.ssaika.ssiren.global.entity.BaseTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "user_consents")
public class UserConsent extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "location_agreed", nullable = false)
    private Boolean locationAgreed;

    @Column(name = "sensitive_info_agreed", nullable = false)
    private Boolean sensitiveInfoAgreed;

    @Column(name = "sensitive_info_agreed_at")
    private LocalDateTime sensitiveInfoAgreedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public static UserConsent create(User user, Boolean locationAgreed, Boolean sensitiveInfoAgreed) {
        return UserConsent.builder()
            .user(user)
            .locationAgreed(locationAgreed)
            .sensitiveInfoAgreed(sensitiveInfoAgreed)
            .sensitiveInfoAgreedAt(Boolean.TRUE.equals(sensitiveInfoAgreed) ? LocalDateTime.now() : null)
            .build();
    }

    public void update(Boolean locationAgreed, Boolean sensitiveInfoAgreed) {
        this.locationAgreed = locationAgreed;
        this.sensitiveInfoAgreedAt = resolveSensitiveInfoAgreedAt(sensitiveInfoAgreed);
        this.sensitiveInfoAgreed = sensitiveInfoAgreed;
    }

    private LocalDateTime resolveSensitiveInfoAgreedAt(Boolean nextSensitiveInfoAgreed) {
        if (Boolean.TRUE.equals(nextSensitiveInfoAgreed)) {
            if (Boolean.TRUE.equals(this.sensitiveInfoAgreed)) {
                return this.sensitiveInfoAgreedAt;
            }
            return LocalDateTime.now();
        }
        return null;
    }
}
