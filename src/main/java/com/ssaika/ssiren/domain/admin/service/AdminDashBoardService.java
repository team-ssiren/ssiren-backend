package com.ssaika.ssiren.domain.admin.service;

import com.ssaika.ssiren.domain.admin.dto.projection.AdminDashboardIssueGroupPointProjection;
import com.ssaika.ssiren.domain.admin.dto.request.AdminDashboardDenseAreaRequest;
import com.ssaika.ssiren.domain.admin.dto.response.*;
import com.ssaika.ssiren.domain.admin.repsitory.AdminDashboardQueryRepository;
import com.ssaika.ssiren.domain.user.entity.OfficerDepartment;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.repository.OfficerDepartmentRepository;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.enums.UserRole;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashBoardService {

    private static final List<Long> EMPTY_SCOPE_DEPARTMENT_IDS = List.of(-1L);
    private static final List<Long> EMPTY_SCOPE_AGENCY_TYPE_IDS = List.of(-1L);

    private final UserRepository userRepository;
    private final OfficerDepartmentRepository officerDepartmentRepository;
    private final AdminDashboardQueryRepository adminDashboardQueryRepository;

    public AdminDashboardStatisticsResponse getStatistics(Long userId, Boolean myDepartmentOnly) {
        User user = getOfficer(userId);
        DashboardScope scope = resolveScope(user, myDepartmentOnly);

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = today.withDayOfMonth(1).plusMonths(1).atStartOfDay();
        LocalDateTime delayThreshold = LocalDateTime.now().minusDays(7);

        return AdminDashboardStatisticsResponse.from(
                adminDashboardQueryRepository.getStatistics(
                        scope.myDepartmentOnly(),
                        scope.departmentIdsForQuery(),
                        scope.agencyTypeIdsForQuery(),
                        delayThreshold,
                        monthStart,
                        nextMonthStart,
                        todayStart,
                        tomorrowStart
                )
        );
    }

    public AdminDashboardCategoryStatisticsResponse getCategoryStatistics(Long userId, Boolean myDepartmentOnly) {
        User user = getOfficer(userId);
        DashboardScope scope = resolveScope(user, myDepartmentOnly);

        return AdminDashboardCategoryStatisticsResponse.from(
                adminDashboardQueryRepository.getCategoryStatistics(
                        scope.myDepartmentOnly(),
                        scope.departmentIdsForQuery(),
                        scope.agencyTypeIdsForQuery()
                )
        );
    }

    public AdminDashboardDenseAreaResponse getDenseAreas(
            Long userId,
            AdminDashboardDenseAreaRequest request
    ) {
        User user = getOfficer(userId);
        DashboardScope scope = resolveScope(user, request.myDepartmentOnly());

        List<AdminDashboardIssueGroupPointProjection> points =
                adminDashboardQueryRepository.findIssueGroupPointsForDenseArea(
                        scope.myDepartmentOnly(),
                        scope.departmentIdsForQuery(),
                        scope.agencyTypeIdsForQuery(),
                        request.latitude(),
                        request.longitude(),
                        request.radiusMeters()
                );

        double latitude = request.latitude().doubleValue();
        double longitude = request.longitude().doubleValue();
        int gridSizeMeters = request.resolvedGridSizeMeters();
        int minIssueGroupCount = request.resolvedMinIssueGroupCount();

        double latDegreePerMeter = 1.0 / 111_320.0;
        double lngDegreePerMeter = 1.0 / (111_320.0 * Math.cos(Math.toRadians(latitude)));
        double latDelta = request.radiusMeters() * latDegreePerMeter;
        double lngDelta = request.radiusMeters() * lngDegreePerMeter;
        double swLat = latitude - latDelta;
        double swLng = longitude - lngDelta;
        double gridLatSize = gridSizeMeters * latDegreePerMeter;
        double gridLngSize = gridSizeMeters * lngDegreePerMeter;

        Map<GridKey, List<AdminDashboardIssueGroupPointProjection>> groupedPoints = points.stream()
                .collect(Collectors.groupingBy(point -> {
                    int row = (int) Math.floor((point.getGroupLatitude().doubleValue() - swLat) / gridLatSize);
                    int col = (int) Math.floor((point.getGroupLongitude().doubleValue() - swLng) / gridLngSize);
                    return new GridKey(row, col);
                }));

        List<AdminDashboardDenseAreaItemResponse> denseAreas = groupedPoints.entrySet()
                .stream()
                .filter(entry -> entry.getValue().size() >= minIssueGroupCount)
                .map(entry -> toDenseAreaItem(entry.getKey(), entry.getValue(), swLat, swLng,
                        gridLatSize, gridLngSize))
                .sorted((left, right) -> Long.compare(right.issueGroupCount(), left.issueGroupCount()))
                .toList();

        return AdminDashboardDenseAreaResponse.from(denseAreas);
    }

    private User getOfficer(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED.getMessage(), ErrorCode.UNAUTHORIZED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                        ErrorCode.USER_NOT_FOUND));

        if (user.getRole() != UserRole.OFFICER) {
            throw new CustomException(ErrorCode.FORBIDDEN.getMessage(), ErrorCode.FORBIDDEN);
        }

        return user;
    }

    private DashboardScope resolveScope(User user, Boolean myDepartmentOnly) {
        List<OfficerDepartment> officerDepartments = officerDepartmentRepository.findByUserId(user.getId());

        List<Long> departmentIds = officerDepartments.stream()
                .map(OfficerDepartment::getDepartment)
                .map(department -> department.getId())
                .distinct()
                .toList();

        List<Long> agencyTypeIds = officerDepartments.stream()
                .map(OfficerDepartment::getDepartment)
                .map(department -> department.getAgencyType().getId())
                .distinct()
                .toList();

        if (departmentIds.isEmpty() || agencyTypeIds.isEmpty()) {
            throw new CustomException(ErrorCode.FORBIDDEN.getMessage(), ErrorCode.FORBIDDEN);
        }

        return new DashboardScope(Boolean.TRUE.equals(myDepartmentOnly), departmentIds, agencyTypeIds);
    }

    private record DashboardScope(
            boolean myDepartmentOnly,
            List<Long> departmentIds,
            List<Long> agencyTypeIds
    ) {

        private List<Long> departmentIdsForQuery() {
            return departmentIds == null || departmentIds.isEmpty()
                    ? EMPTY_SCOPE_DEPARTMENT_IDS
                    : departmentIds;
        }

        private List<Long> agencyTypeIdsForQuery() {
            return agencyTypeIds == null || agencyTypeIds.isEmpty()
                    ? EMPTY_SCOPE_AGENCY_TYPE_IDS
                    : agencyTypeIds;
        }
    }

    private AdminDashboardDenseAreaItemResponse toDenseAreaItem(
            GridKey key,
            List<AdminDashboardIssueGroupPointProjection> points,
            double baseSwLat,
            double baseSwLng,
            double gridLatSize,
            double gridLngSize
    ) {
        double cellSwLat = baseSwLat + key.row() * gridLatSize;
        double cellSwLng = baseSwLng + key.col() * gridLngSize;
        double cellNeLat = cellSwLat + gridLatSize;
        double cellNeLng = cellSwLng + gridLngSize;

        double centerLat = points.stream()
                .mapToDouble(point -> point.getGroupLatitude().doubleValue())
                .average()
                .orElse((cellSwLat + cellNeLat) / 2.0);

        double centerLng = points.stream()
                .mapToDouble(point -> point.getGroupLongitude().doubleValue())
                .average()
                .orElse((cellSwLng + cellNeLng) / 2.0);

        return new AdminDashboardDenseAreaItemResponse(
                (long) points.size(),
                BigDecimal.valueOf(centerLat),
                BigDecimal.valueOf(centerLng),
                new AdminDashboardDenseAreaBoundsResponse(
                        BigDecimal.valueOf(cellSwLat),
                        BigDecimal.valueOf(cellSwLng),
                        BigDecimal.valueOf(cellNeLat),
                        BigDecimal.valueOf(cellNeLng)
                )
        );
    }

    private record GridKey(
            int row,
            int col
    ) {
    }
}
