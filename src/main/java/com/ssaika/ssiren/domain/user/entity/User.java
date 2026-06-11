package com.ssaika.ssiren.domain.user.entity;

import com.ssaika.ssiren.global.entity.BaseTime;
import com.ssaika.ssiren.global.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "users")
public class User extends BaseTime {

    private static final int MAX_NICKNAME_LENGTH = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "is_alarm_enabled", nullable = false)
    private Boolean isAlarmEnabled;

    @Column(name = "role_selected", nullable = false)
    private Boolean roleSelected;

    public static User createKakaoUser(String email, String nickname) {
        return User.builder()
            .email(email)
            .nickname(normalizeNickname(email, nickname))
            .role(UserRole.CITIZEN)
            .isActive(true)
            .isAlarmEnabled(false)
            .roleSelected(false)
            .build();
    }

    public void updateProfile(String nickname, Boolean isAlarmEnabled) {
        if (nickname != null) {
            this.nickname = normalizeNickname(email, nickname);
        }
        if (isAlarmEnabled != null) {
            this.isAlarmEnabled = isAlarmEnabled;
        }
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void updateRole(UserRole role) {
        this.role = role;
        this.roleSelected = true;
    }

    private static String normalizeNickname(String email, String nickname) {
        String value = nickname == null || nickname.isBlank() ? email : nickname;
        return value.length() > MAX_NICKNAME_LENGTH ? value.substring(0, MAX_NICKNAME_LENGTH) : value;
    }
}
