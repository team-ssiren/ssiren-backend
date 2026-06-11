package com.ssaika.ssiren.domain.user.service;

import com.ssaika.ssiren.domain.agency.entity.Department;
import com.ssaika.ssiren.domain.agency.repository.DepartmentRepository;
import com.ssaika.ssiren.domain.user.dto.request.UserConsentUpdateRequest;
import com.ssaika.ssiren.domain.user.dto.request.UserRoleUpdateRequest;
import com.ssaika.ssiren.domain.user.dto.request.UserUpdateRequest;
import com.ssaika.ssiren.domain.user.dto.response.UserConsentResponse;
import com.ssaika.ssiren.domain.user.dto.response.UserResponse;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.entity.UserConsent;
import com.ssaika.ssiren.domain.user.entity.OfficerDepartment;
import com.ssaika.ssiren.domain.user.repository.OfficerDepartmentRepository;
import com.ssaika.ssiren.domain.user.repository.UserConsentRepository;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.enums.UserRole;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserConsentRepository userConsentRepository;
    private final OfficerDepartmentRepository officerDepartmentRepository;
    private final DepartmentRepository departmentRepository;

    public UserResponse getUserById(Long userId) {
        User user = findUserById(userId);

        return UserResponse.from(user);
    }

    @Transactional
    public UserConsentResponse getUserConsent(Long userId) {
        User user = findUserById(userId);
        UserConsent userConsent = getOrCreateDefaultConsent(user);

        return UserConsentResponse.from(userConsent);
    }

    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = findUserById(userId);

        user.updateProfile(request.getNickname(), request.getIsAlarmEnabled());
        userRepository.flush();

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse updateUserRole(Long requesterId, Long targetUserId, UserRoleUpdateRequest request) {
        validateRoleUpdateAccess(requesterId, targetUserId);

        User user = findUserById(targetUserId);
        UserRole role = request.getRole();

        if (role != UserRole.CITIZEN && role != UserRole.OFFICER) {
            throw new CustomException(ErrorCode.USER_ROLE_TYPE_MISMATCH.getMessage(),
                ErrorCode.USER_ROLE_TYPE_MISMATCH);
        }

        officerDepartmentRepository.deleteByUserId(targetUserId);
        user.updateRole(role);

        if (role == UserRole.OFFICER) {
            if (request.getDepartmentId() == null) {
                throw new CustomException("공무원 역할에는 소속 부서가 필요합니다.",
                    ErrorCode.MISSING_PARAMETER);
            }

            Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new CustomException("소속 부서를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
            officerDepartmentRepository.save(OfficerDepartment.create(user, department));
        }

        userRepository.flush();

        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse deactivateUser(Long userId) {
        User user = findUserById(userId);

        user.deactivate();
        userRepository.flush();

        return UserResponse.from(user);
    }

    @Transactional
    public UserConsentResponse updateUserConsent(
        Long userId,
        UserConsentUpdateRequest request) {
        User user = findUserById(userId);

        UserConsent userConsent = userConsentRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId)
            .map(consent -> {
                consent.update(
                    request.getLocationAgreed(),
                    request.getSensitiveInfoAgreed());
                return consent;
            })
            .orElseGet(() -> userConsentRepository.save(UserConsent.create(
                user,
                request.getLocationAgreed(),
                request.getSensitiveInfoAgreed()
            )));

        userConsentRepository.flush();

        return UserConsentResponse.from(userConsent);
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                ErrorCode.USER_NOT_FOUND));
    }

    private void validateRoleUpdateAccess(Long requesterId, Long targetUserId) {
        if (requesterId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED.getMessage(), ErrorCode.UNAUTHORIZED);
        }
        if (requesterId.equals(targetUserId)) {
            return;
        }

        User requester = findUserById(requesterId);
        if (requester.getRole() != UserRole.ADMIN) {
            throw new CustomException(ErrorCode.FORBIDDEN.getMessage(), ErrorCode.FORBIDDEN);
        }
    }

    private UserConsent getOrCreateDefaultConsent(User user) {
        return userConsentRepository.findFirstByUserIdOrderByUpdatedAtDesc(user.getId())
            .orElseGet(() -> userConsentRepository.save(UserConsent.create(user, false, false)));
    }
}
