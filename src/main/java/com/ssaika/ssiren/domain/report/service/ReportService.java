package com.ssaika.ssiren.domain.report.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.ssaika.ssiren.domain.agency.entity.Department;
import com.ssaika.ssiren.domain.agency.repository.DepartmentRepository;
import com.ssaika.ssiren.domain.report.address.AddressResolver;
import com.ssaika.ssiren.domain.report.address.AddressSnapshot;
import com.ssaika.ssiren.domain.report.client.ReportAiClient;
import com.ssaika.ssiren.domain.report.client.dto.request.ReportAiAnalyzeRequest;
import com.ssaika.ssiren.domain.report.client.dto.response.ReportAiAnalyzeResponse;
import com.ssaika.ssiren.domain.report.dto.request.MyReportUpdateRequest;
import com.ssaika.ssiren.domain.report.dto.request.ReportCreateRequest;
import com.ssaika.ssiren.domain.report.dto.request.ReportDraftRequest;
import com.ssaika.ssiren.domain.report.dto.request.ReportReactionRequest;
import com.ssaika.ssiren.domain.report.dto.response.*;
import com.ssaika.ssiren.domain.report.entity.*;
import com.ssaika.ssiren.domain.report.repository.*;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.enums.IssueGroupStatus;
import com.ssaika.ssiren.global.enums.ReportReactionType;
import com.ssaika.ssiren.global.enums.ReportStatus;
import com.ssaika.ssiren.global.enums.ReportVisibility;
import com.ssaika.ssiren.global.exception.CustomException;
import com.ssaika.ssiren.global.exception.ErrorCode;
import com.ssaika.ssiren.global.util.ReportImageStorage;
import com.ssaika.ssiren.global.util.ReportImageStorage.UploadedReportImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final BigDecimal MIN_LATITUDE = BigDecimal.valueOf(-90);
    private static final BigDecimal MAX_LATITUDE = BigDecimal.valueOf(90);
    private static final BigDecimal MIN_LONGITUDE = BigDecimal.valueOf(-180);
    private static final BigDecimal MAX_LONGITUDE = BigDecimal.valueOf(180);
    private static final int MAX_REPORT_DRAFT_IMAGE_COUNT = 5;
    private static final long MAX_REPORT_DRAFT_IMAGE_SIZE = 50 * 1024 * 1024;
    private static final int REPORT_EMBEDDING_DIMENSION = 1536;
    private static final double EARTH_RADIUS_METERS = 6_371_000;
    private static final String ADDRESS_NOT_RESOLVED = "주소 확인 필요";
    private static final String INSUFFICIENT_CATEGORY_CODE = "INSUFFICIENT";

    private final IssueGroupStatsService issueGroupStatsService;
    private final ReportRepository reportRepository;
    private final IssueGroupRepository issueGroupRepository;
    private final ReportCategoryRepository reportCategoryRepository;
    private final ReportCategoryMergeRuleRepository reportCategoryMergeRuleRepository;
    private final ReportImageRepository reportImageRepository;
    private final ReportStatusHistoryRepository reportStatusHistoryRepository;
    private final ReportReactionLogRepository reportReactionLogRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final ObjectMapper objectMapper;
    private final ReportAiClient reportAiClient;
    private final AddressResolver addressResolver;
    private final ReportImageStorage reportImageStorage;

    public ReportDraftCreateResponse createReportDraft(Long userId, ReportDraftRequest request) {
        validateAuthenticatedUser(userId);
        validateReportDraftRequest(request);
        log.info(
            "Create report draft. userId={}, latitude={}, longitude={}, imageCount={}",
            userId,
            request.latitude(),
            request.longitude(),
            request.images() == null ? 0 : request.images().size()
        );

        userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(), ErrorCode.USER_NOT_FOUND));

        LocalDateTime occurredAt = request.occurredAt() == null ? LocalDateTime.now() : request.occurredAt();
        AddressSnapshot address = addressResolver.resolve(request.latitude(), request.longitude());
        ReportAiAnalyzeResponse aiResponse = reportAiClient.analyzeReport(new ReportAiAnalyzeRequest(
            request.content(),
            request.latitude(),
            request.longitude(),
            occurredAt,
            address.roadAddress(),
            address.sido(),
            address.sigungu(),
            address.eupmyeondong(),
            request.images()
        ));

        ReportCategory category = resolveReportCategory(aiResponse);

        return new ReportDraftCreateResponse(
            createReportDraftResponse(
                userId,
                request,
                occurredAt,
                address,
                aiResponse,
                category
            ),
            ReportCategoryResponse.from(category),
            category.getParentCategory() == null ? null : ReportCategoryResponse.from(category.getParentCategory()),
            ReportDepartmentResponse.from(category.getDepartment()),
            ReportAgencyTypeResponse.from(category.getDepartment().getAgencyType()),
            ReportAiAnalysisResponse.from(aiResponse.analysis())
        );
    }

    @Transactional
    public ReportCreateResponse createReport(
        Long userId,
        ReportCreateRequest request,
        List<MultipartFile> images) {
        validateAuthenticatedUser(userId);
        validateReportCreateRequest(request, images);
        log.info(
            "Create report. userId={}, categoryId={}, departmentId={}, latitude={}, longitude={}, imageCount={}",
            userId,
            request.categoryId(),
            request.departmentId(),
            request.latitude(),
            request.longitude(),
            images == null ? 0 : images.size()
        );

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(), ErrorCode.USER_NOT_FOUND));
        ReportCategory category = getReportCategory(request.categoryId());
        validateRegistrableCategory(category);
        Department department = getDepartment(request.departmentId());
        validateCategoryDepartment(category, department);

        IssueGroup issueGroup = resolveIssueGroupForCreate(request);

        Report report = reportRepository.saveAndFlush(Report.create(
            request.title(),
            convertContents(request.contents()),
            request.latitude(),
            request.longitude(),
            request.roadAddress(),
            request.jibunAddress(),
            request.sido(),
            request.sigungu(),
            request.eupmyeondong(),
            request.occurredAt(),
            request.riskScore(),
            request.visibility(),
            toPgVector(request.embedding()),
            issueGroup.getReportCount() == 1,
            user,
            category,
            issueGroup,
            department
        ));

        List<ReportImage> reportImages = uploadAndSaveReportImages(userId, report, images);

        return ReportCreateResponse.from(report, reportImages, issueGroup, objectMapper);
    }

    public List<DuplicateReportCandidate> findDuplicateReportCandidates(ReportCreateRequest request) {
        validateReportCreateRequest(request, null);
        if (request.embedding() == null || request.embedding().isEmpty()) {
            return List.of();
        }

        ReportCategoryMergeRule mergeRule = reportCategoryMergeRuleRepository.findByCategory_Id(request.categoryId())
            .orElseThrow(() -> new CustomException("카테고리 병합 기준을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));

        return reportRepository.findDuplicateCandidates(
            request.categoryId(),
            request.departmentId(),
            request.latitude(),
            request.longitude(),
            toPgVector(request.embedding()),
            mergeRule.getLinkRadiusMeters(),
            mergeRule.getMinEmbeddingSimilarity()
        );
    }

    private IssueGroup resolveIssueGroupForCreate(ReportCreateRequest request) {
        String embedding = toPgVector(request.embedding());
        if (embedding == null) {
            return createNewIssueGroup(request);
        }

        ReportCategoryMergeRule mergeRule = reportCategoryMergeRuleRepository.findByCategory_Id(request.categoryId())
            .orElseThrow(() -> new CustomException("카테고리 병합 기준을 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
        List<DuplicateReportCandidate> candidates = reportRepository.findDuplicateCandidates(
            request.categoryId(),
            request.departmentId(),
            request.latitude(),
            request.longitude(),
            embedding,
            mergeRule.getLinkRadiusMeters(),
            mergeRule.getMinEmbeddingSimilarity()
        );

        return selectMergeTarget(request, mergeRule, candidates)
            .map(target -> mergeIssueGroup(request, target))
            .orElseGet(() -> createNewIssueGroup(request));
    }

    private java.util.Optional<MergeTarget> selectMergeTarget(
        ReportCreateRequest request,
        ReportCategoryMergeRule mergeRule,
        List<DuplicateReportCandidate> candidates) {
        if (candidates.isEmpty()) {
            return java.util.Optional.empty();
        }

        Map<Long, IssueGroup> issueGroups = issueGroupRepository.findAllById(candidateIssueGroupIds(candidates))
            .stream()
            .collect(Collectors.toMap(IssueGroup::getId, Function.identity()));
        Map<Long, List<Report>> reportsByIssueGroup = reportRepository
            .findByIssueGroup_IdInAndIsDeletedFalse(issueGroups.keySet())
            .stream()
            .collect(Collectors.groupingBy(report -> report.getIssueGroup().getId()));

        return candidates.stream()
            .map(candidate -> createMergeTarget(request, mergeRule, candidate, issueGroups, reportsByIssueGroup))
            .filter(Objects::nonNull)
            .filter(target -> target.score().compareTo(mergeRule.getAutoMergeThreshold()) >= 0)
            .max(Comparator.comparing(MergeTarget::score));
    }

    private Set<Long> candidateIssueGroupIds(List<DuplicateReportCandidate> candidates) {
        return candidates.stream()
            .map(DuplicateReportCandidate::getCandidateIssueGroupId)
            .filter(Objects::nonNull)
            .collect(Collectors.toCollection(HashSet::new));
    }

    private MergeTarget createMergeTarget(
        ReportCreateRequest request,
        ReportCategoryMergeRule mergeRule,
        DuplicateReportCandidate candidate,
        Map<Long, IssueGroup> issueGroups,
        Map<Long, List<Report>> reportsByIssueGroup) {
        IssueGroup issueGroup = issueGroups.get(candidate.getCandidateIssueGroupId());
        if (issueGroup == null) {
            return null;
        }

        List<Report> groupReports = reportsByIssueGroup.getOrDefault(issueGroup.getId(), List.of());
        BigDecimal newGroupDiameter = calculateNewGroupDiameter(request, issueGroup, groupReports);
        BigDecimal maxGroupDiameter = BigDecimal.valueOf(mergeRule.getMaxGroupDiameterMeters());
        if (newGroupDiameter.compareTo(maxGroupDiameter) > 0) {
            return null;
        }

        BigDecimal score = calculateMergeScore(candidate, mergeRule, newGroupDiameter);
        return new MergeTarget(issueGroup, groupReports, newGroupDiameter, score);
    }

    private IssueGroup mergeIssueGroup(ReportCreateRequest request, MergeTarget target) {
        Coordinate center = calculateGroupCenter(request, target.groupReports());
        target.issueGroup().mergeReport(
            center.latitude(),
            center.longitude(),
            target.newGroupDiameter(),
            request.riskScore(),
            LocalDateTime.now()
        );
        return target.issueGroup();
    }

    private IssueGroup createNewIssueGroup(ReportCreateRequest request) {
        return issueGroupRepository.save(IssueGroup.create(
            request.title(),
            resolveIssueGroupContent(request.contents()),
            request.latitude(),
            request.longitude(),
            LocalDateTime.now(),
            request.riskScore()
        ));
    }

    private BigDecimal calculateNewGroupDiameter(
        ReportCreateRequest request,
        IssueGroup issueGroup,
        List<Report> groupReports) {
        BigDecimal currentDiameter = issueGroup.getGroupDiameterMeters() == null
            ? BigDecimal.ZERO
            : issueGroup.getGroupDiameterMeters();
        BigDecimal maxDistanceFromNewReport = groupReports.stream()
            .map(report -> calculateDistanceMeters(
                request.latitude(),
                request.longitude(),
                report.getLatitude(),
                report.getLongitude()
            ))
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);

        return currentDiameter.max(maxDistanceFromNewReport);
    }

    private BigDecimal calculateMergeScore(
        DuplicateReportCandidate candidate,
        ReportCategoryMergeRule mergeRule,
        BigDecimal newGroupDiameter) {
        double distance = safeDouble(candidate.getDistanceMeters());
        double similarity = safeDouble(candidate.getEmbeddingSimilarity());
        double linkRadius = mergeRule.getLinkRadiusMeters();
        double maxDiameter = mergeRule.getMaxGroupDiameterMeters();
        double diameter = safeDouble(newGroupDiameter);

        double distanceScore = 30.0 * Math.max(0.0, 1.0 - distance / linkRadius);
        double embeddingScore = 50.0 * Math.max(0.0, Math.min(1.0, similarity));
        double diameterScore = 20.0 * Math.max(0.0, 1.0 - diameter / maxDiameter);

        return BigDecimal.valueOf(distanceScore + embeddingScore + diameterScore)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateDistanceMeters(
        BigDecimal latitude1,
        BigDecimal longitude1,
        BigDecimal latitude2,
        BigDecimal longitude2) {
        double lat1 = Math.toRadians(latitude1.doubleValue());
        double lat2 = Math.toRadians(latitude2.doubleValue());
        double deltaLat = lat2 - lat1;
        double deltaLon = Math.toRadians(longitude2.doubleValue() - longitude1.doubleValue());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2)
            + Math.cos(lat1) * Math.cos(lat2)
            * Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return BigDecimal.valueOf(EARTH_RADIUS_METERS * c).setScale(2, RoundingMode.HALF_UP);
    }

    private Coordinate calculateGroupCenter(ReportCreateRequest request, List<Report> groupReports) {
        BigDecimal latitudeSum = request.latitude();
        BigDecimal longitudeSum = request.longitude();
        for (Report report : groupReports) {
            latitudeSum = latitudeSum.add(report.getLatitude());
            longitudeSum = longitudeSum.add(report.getLongitude());
        }

        BigDecimal count = BigDecimal.valueOf(groupReports.size() + 1L);
        return new Coordinate(
            latitudeSum.divide(count, 7, RoundingMode.HALF_UP),
            longitudeSum.divide(count, 7, RoundingMode.HALF_UP)
        );
    }

    private double safeDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    public List<IssueResponse> getIssues(
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters,
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng,
        Long categoryId,
        Long agencyId,
        IssueGroupStatus status,
        BigDecimal riskMin,
        BigDecimal riskMax,
        String from,
        String to) {
        validateRadiusParameters(latitude, longitude, radiusMeters);
        validateBoundsParameters(swLat, swLng, neLat, neLng);
        validateRiskRange(riskMin, riskMax);
        validateBoundsRange(swLat, swLng, neLat, neLng);
        LocalDateTime fromDateTime = parseFromDateTime(from);
        LocalDateTime toDateTime = parseToDateTime(to);
        validateDateRange(fromDateTime, toDateTime);
        log.info(
            "Get issues. latitude={}, longitude={}, radiusMeters={}, swLat={}, swLng={}, "
                + "neLat={}, neLng={}, categoryId={}, agencyId={}, status={}, riskMin={}, "
                + "riskMax={}, from={}, to={}",
            latitude,
            longitude,
            radiusMeters,
            swLat,
            swLng,
            neLat,
            neLng,
            categoryId,
            agencyId,
            status,
            riskMin,
            riskMax,
            fromDateTime,
            toDateTime
        );

        Specification<Report> specification = ReportSpecification.isRepresentative()
            .and(ReportSpecification.hasVisibility(ReportVisibility.PUBLIC))
            .and(ReportSpecification.isNotDeleted())
            .and(ReportSpecification.hasCategory(categoryId))
            .and(ReportSpecification.hasAgencyType(agencyId))
            .and(ReportSpecification.hasIssueGroupStatus(status))
            .and(ReportSpecification.issueGroupRiskScoreFrom(riskMin))
            .and(ReportSpecification.issueGroupRiskScoreTo(riskMax))
            .and(ReportSpecification.recentReportedAtFrom(fromDateTime))
            .and(ReportSpecification.recentReportedAtTo(toDateTime))
            .and(ReportSpecification.issueGroupInBounds(swLat, swLng, neLat, neLng))
            .and(ReportSpecification.issueGroupWithinRadius(latitude, longitude, radiusMeters));

        return reportRepository.findAll(specification, Sort.by(Sort.Direction.DESC, "issueGroup.riskScore"))
            .stream()
            .map(report -> IssueResponse.from(report, objectMapper))
            .toList();
    }

    public IssueDetailResponse getIssue(Long issueGroupId) {
        log.info("Get issue detail. issueGroupId={}", issueGroupId);

        IssueGroup issueGroup = issueGroupRepository.findById(issueGroupId)
            .orElseThrow(() -> new CustomException("?댁뒋 洹몃９???李얠쓣 ???놁뒿?덈떎.", ErrorCode.NOT_FOUND));
        Specification<Report> specification = ReportSpecification.hasIssueGroup(issueGroupId)
            .and(ReportSpecification.hasVisibility(ReportVisibility.PUBLIC))
            .and(ReportSpecification.isNotDeleted());
        List<Report> reports = reportRepository.findAll(
            specification,
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Report representativeReport = reports.stream()
            .filter(Report::getIsRepresentative)
            .findFirst()
            .orElseGet(() -> reports.isEmpty() ? null : reports.get(0));

        return IssueDetailResponse.from(issueGroup, representativeReport, reports, objectMapper);
    }

    public Page<ReportListResponse> getReports(
        ReportStatus status,
        Long categoryId,
        String sido,
        String sigungu,
        String eupmyeondong,
        String from,
        String to,
        Pageable pageable) {
        LocalDateTime fromDateTime = parseFromDateTime(from);
        LocalDateTime toDateTime = parseToDateTime(to);
        validateDateRange(fromDateTime, toDateTime);
        log.info(
            "Get public reports. status={}, categoryId={}, sido={}, sigungu={}, eupmyeondong={}, "
                + "from={}, to={}, page={}, size={}",
            status,
            categoryId,
            sido,
            sigungu,
            eupmyeondong,
            fromDateTime,
            toDateTime,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );

        Specification<Report> specification = ReportSpecification.hasVisibility(ReportVisibility.PUBLIC)
            .and(ReportSpecification.isNotDeleted())
            .and(ReportSpecification.hasStatus(status))
            .and(ReportSpecification.hasCategory(categoryId))
            .and(ReportSpecification.hasSido(sido))
            .and(ReportSpecification.hasSigungu(sigungu))
            .and(ReportSpecification.hasEupmyeondong(eupmyeondong))
            .and(ReportSpecification.createdAtFrom(fromDateTime))
            .and(ReportSpecification.createdAtTo(toDateTime));

        Page<Report> reports = reportRepository.findAll(specification, pageable);
        Map<Long, List<ReportImage>> reportImages = getReportImages(reports.getContent());

        return reports.map(report -> ReportListResponse.from(
            report,
            reportImages.getOrDefault(report.getId(), List.of()),
            objectMapper
        ));
    }

    public Page<MyReportResponse> getMyReports(
        Long userId,
        ReportStatus status,
        Long categoryId,
        String from,
        String to,
        Pageable pageable) {
        LocalDateTime fromDateTime = parseFromDateTime(from);
        LocalDateTime toDateTime = parseToDateTime(to);
        validateDateRange(fromDateTime, toDateTime);
        log.info(
            "Get my reports. userId={}, status={}, categoryId={}, from={}, to={}, page={}, size={}",
            userId,
            status,
            categoryId,
            fromDateTime,
            toDateTime,
            pageable.getPageNumber(),
            pageable.getPageSize()
        );

        Specification<Report> specification = ReportSpecification.belongsToUser(userId)
            .and(ReportSpecification.hasStatus(status))
            .and(ReportSpecification.hasCategory(categoryId))
            .and(ReportSpecification.createdAtFrom(fromDateTime))
            .and(ReportSpecification.createdAtTo(toDateTime));

        Page<Report> reports = reportRepository.findAll(specification, pageable);
        Map<Long, List<ReportImage>> reportImages = getReportImages(reports.getContent());

        return reports.map(report -> MyReportResponse.from(
            report,
            reportImages.getOrDefault(report.getId(), List.of()),
            objectMapper
        ));
    }

    public MyReportDetailResponse getMyReport(Long userId, Long reportId) {
        log.info("Get my report detail. userId={}, reportId={}", userId, reportId);

        Report report = reportRepository.findByIdAndUser_Id(reportId, userId)
            .orElseThrow(() -> new CustomException("제보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
        List<ReportImage> reportImages = reportImageRepository.findByReport_IdOrderBySortOrderAsc(reportId);
        List<ReportStatusHistory> statusHistories =
            reportStatusHistoryRepository.findByReport_IdOrderByCreatedAtAsc(reportId);
        List<ReportReactionLog> reactionLogs =
            reportReactionLogRepository.findByReport_IdOrderByCreatedAtAsc(reportId);

        return MyReportDetailResponse.from(
            report,
            reportImages,
            statusHistories,
            reactionLogs,
            objectMapper
        );
    }

    @Transactional
    public MyReportUpdateResponse updateMyReport(
        Long userId,
        Long reportId,
        MyReportUpdateRequest request) {
        log.info("Update my report. userId={}, reportId={}", userId, reportId);

        Report report = reportRepository.findByIdAndUser_Id(reportId, userId)
            .orElseThrow(() -> new CustomException("제보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
        validateUpdatableReport(report);
        validateUnchangedCategory(report, request.categoryId());

        report.update(
            request.title(),
            convertContents(request.contents()),
            request.visibility()
        );
        if (Boolean.TRUE.equals(report.getIsRepresentative())) {
            report.getIssueGroup().syncRepresentativeReport(
                report.getTitle(),
                resolveIssueGroupContent(parseReportContents(report))
            );
        }
        reportRepository.flush();

        return MyReportUpdateResponse.from(report, objectMapper);
    }

    @Transactional
    public MyReportDeleteResponse deleteMyReport(Long userId, Long reportId) {
        log.info("Delete my report. userId={}, reportId={}", userId, reportId);

        Report report = reportRepository.findByIdAndUser_Id(reportId, userId)
            .orElseThrow(() -> new CustomException("?쒕낫瑜?李얠쓣 ???놁뒿?덈떎.", ErrorCode.NOT_FOUND));
        List<ReportImage> reportImages = reportImageRepository.findByReport_IdOrderBySortOrderAsc(reportId);
        IssueGroup issueGroup = report.getIssueGroup();
        List<Report> remainingReports = reportRepository.findByIssueGroup_IdAndIsDeletedFalse(issueGroup.getId())
            .stream()
            .filter(groupReport -> !groupReport.getId().equals(report.getId()))
            .toList();

        if (!remainingReports.isEmpty()) {
            issueGroupStatsService.refreshIssueGroupByReports(issueGroup, remainingReports);
        }
        MyReportDeleteResponse response = MyReportDeleteResponse.from(
            report,
            reportImages,
            remainingReports.isEmpty() ? null : issueGroup,
            objectMapper
        );

        reportRepository.delete(report);
        reportRepository.flush();
        if (remainingReports.isEmpty()) {
            issueGroupRepository.delete(issueGroup);
            issueGroupRepository.flush();
        }
        deleteReportImagesAfterCommit(reportImages);

        return response;
    }

    @Transactional
    public ReportReactionResponse saveReportReaction(
        Long userId,
        Long reportId,
        ReportReactionRequest request) {
        log.info(
            "Save report reaction. userId={}, reportId={}, reactionType={}",
            userId,
            reportId,
            request.reactionType()
        );

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND.getMessage(),
                ErrorCode.USER_NOT_FOUND));
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new CustomException("?쒕낫瑜?李얠쓣 ???놁뒿?덈떎.", ErrorCode.NOT_FOUND));

        IssueGroup issueGroup = report.getIssueGroup();

        ReportReactionLog reactionLog = reportReactionLogRepository
            .findByReport_IdAndUser_Id(reportId, userId)
            .map(existingReactionLog -> {
                ReportReactionType previousReactionType =
                    existingReactionLog.updateReactionType(request.reactionType());
                issueGroup.applyReaction(previousReactionType, request.reactionType());
                return existingReactionLog;
            })
            .orElseGet(() -> {
                ReportReactionLog newReactionLog = ReportReactionLog.create(
                    report,
                    user,
                    request.reactionType()
                );
                issueGroup.applyReaction(null, request.reactionType());
                return reportReactionLogRepository.save(newReactionLog);
            });

        issueGroupRepository.saveAndFlush(issueGroup);
        reportReactionLogRepository.flush();

        return ReportReactionResponse.from(reactionLog, report, objectMapper);
    }

    public List<ReportCategoryResponse> getReportCategories() {
        log.info("Get report categories.");

        return reportCategoryRepository.findAllByOrderByIdAsc()
                .stream()
                .filter(category -> !INSUFFICIENT_CATEGORY_CODE.equals(category.getCategoryCode()))
                .map(ReportCategoryResponse::from)
                .toList();
    }

    private LocalDateTime parseFromDateTime(String value) {
        return parseDateTime(value, LocalTime.MIN);
    }

    private LocalDateTime parseToDateTime(String value) {
        return parseDateTime(value, LocalTime.MAX);
    }

    private LocalDateTime parseDateTime(String value, LocalTime defaultTime) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(value).atTime(defaultTime);
            } catch (DateTimeParseException ignored) {
                throw new CustomException("날짜 형식이 올바르지 않습니다.", ErrorCode.INVALID_FORMAT);
            }
        }
    }

    private void validateDateRange(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new CustomException("조회 시작일은 종료일보다 늦을 수 없습니다.", ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateRadiusParameters(
        BigDecimal latitude,
        BigDecimal longitude,
        Integer radiusMeters) {
        boolean hasAnyRadiusParameter =
            latitude != null || longitude != null || radiusMeters != null;
        boolean hasAllRadiusParameters =
            latitude != null && longitude != null && radiusMeters != null;
        if (hasAnyRadiusParameter && !hasAllRadiusParameters) {
            throw new CustomException("諛섍꼍 議고쉶 ?뚮씪誘명꽣媛 遺議깊빀?덈떎.", ErrorCode.MISSING_PARAMETER);
        }
        if (radiusMeters != null && radiusMeters <= 0) {
            throw new CustomException("諛섍꼍???양닔?ъ빞 ?⑸땲??", ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateBoundsParameters(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng) {
        boolean hasAnyBoundsParameter =
            swLat != null || swLng != null || neLat != null || neLng != null;
        boolean hasAllBoundsParameters =
            swLat != null && swLng != null && neLat != null && neLng != null;
        if (hasAnyBoundsParameter && !hasAllBoundsParameters) {
            throw new CustomException("吏???곸뿭 議고쉶 ?뚮씪誘명꽣媛 遺議깊빀?덈떎.", ErrorCode.MISSING_PARAMETER);
        }
    }

    private void validateRiskRange(BigDecimal riskMin, BigDecimal riskMax) {
        if (riskMin != null && riskMax != null && riskMin.compareTo(riskMax) > 0) {
            throw new CustomException("理쒖냼 ?꾪뿕 ?먯닔媛 理쒕? ?꾪뿕 ?먯닔蹂대떎 ?대쓣 ???놁뒿?덈떎.",
                ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateBoundsRange(
        BigDecimal swLat,
        BigDecimal swLng,
        BigDecimal neLat,
        BigDecimal neLng) {
        if (swLat == null || swLng == null || neLat == null || neLng == null) {
            return;
        }
        if (swLat.compareTo(neLat) > 0 || swLng.compareTo(neLng) > 0) {
            throw new CustomException("吏???곸뿭 踰붿쐞媛 ?щ컮瑜댁? ?딆뒿?덈떎.", ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateUpdatableReport(Report report) {
        if (report.getStatus() != ReportStatus.SUBMITTED) {
            throw new CustomException("접수 이후 상태의 제보는 수정할 수 없습니다.", ErrorCode.FORBIDDEN);
        }
    }

    private void validateUnchangedCategory(Report report, Long categoryId) {
        if (categoryId == null) {
            return;
        }
        if (!report.getCategory().getId().equals(categoryId)) {
            throw new CustomException("카테고리 변경은 지원하지 않습니다.", ErrorCode.INVALID_PARAMETER);
        }
    }

    private ReportCategory getReportCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }

        return reportCategoryRepository.findWithDepartmentById(categoryId)
            .orElseThrow(() -> new CustomException("카테고리를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
    }

    private Department getDepartment(Long departmentId) {
        return departmentRepository.findById(departmentId)
            .orElseThrow(() -> new CustomException("담당 부서를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
    }

    private void validateCategoryDepartment(ReportCategory category, Department department) {
        if (!category.getDepartment().getId().equals(department.getId())) {
            throw new CustomException("카테고리와 담당 부서가 일치하지 않습니다.", ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateRegistrableCategory(ReportCategory category) {
        if (INSUFFICIENT_CATEGORY_CODE.equals(category.getCategoryCode())) {
            throw new CustomException(ErrorCode.REPORT_INSUFFICIENT.getMessage(), ErrorCode.REPORT_INSUFFICIENT);
        }
    }

    private String convertContents(JsonNode contents) {
        if (contents == null) {
            return null;
        }
        if (!contents.isObject()) {
            throw new CustomException("제보 본문은 JSON 객체여야 합니다.", ErrorCode.INVALID_FORMAT);
        }

        try {
            return objectMapper.writeValueAsString(contents);
        } catch (JsonProcessingException e) {
            throw new CustomException("제보 본문 형식이 올바르지 않습니다.", ErrorCode.INVALID_FORMAT);
        }
    }

    private JsonNode parseReportContents(Report report) {
        try {
            return objectMapper.readTree(report.getContents());
        } catch (JsonProcessingException e) {
            throw new CustomException("제보 본문 형식이 올바르지 않습니다.", ErrorCode.INVALID_FORMAT);
        }
    }

    private String resolveIssueGroupContent(JsonNode contents) {
        JsonNode summary = contents == null ? null : contents.get("summary");
        if (summary != null && summary.isTextual() && !summary.asText().isBlank()) {
            return summary.asText();
        }
        return contents == null ? null : contents.toString();
    }

    private List<ReportImage> uploadAndSaveReportImages(
        Long userId,
        Report report,
        List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }

        List<String> uploadedObjectKeys = new java.util.ArrayList<>();
        try {
            List<ReportImage> reportImages = new java.util.ArrayList<>();
            int sortOrder = 1;
            for (MultipartFile image : images) {
                if (image == null || image.isEmpty()) {
                    continue;
                }
                UploadedReportImage uploadedImage = reportImageStorage.upload(userId, report.getId(), image);
                uploadedObjectKeys.add(uploadedImage.objectKey());
                reportImages.add(ReportImage.create(uploadedImage.imageUrl(), sortOrder++, report));
            }

            List<ReportImage> savedReportImages = reportImageRepository.saveAll(reportImages);
            deleteUploadedImagesAfterRollback(uploadedObjectKeys);
            return savedReportImages;
        } catch (RuntimeException e) {
            uploadedObjectKeys.forEach(reportImageStorage::deleteQuietly);
            throw e;
        }
    }

    private void deleteReportImagesAfterCommit(List<ReportImage> reportImages) {
        if (reportImages.isEmpty()) {
            return;
        }
        runAfterCommit(() -> reportImages.stream()
            .map(ReportImage::getImageUrl)
            .forEach(reportImageStorage::deleteByImageUrlQuietly));
    }

    private void deleteUploadedImagesAfterRollback(List<String> uploadedObjectKeys) {
        if (uploadedObjectKeys.isEmpty()) {
            return;
        }
        runAfterRollback(() -> uploadedObjectKeys.forEach(reportImageStorage::deleteQuietly));
    }

    private void runAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }

    private void runAfterRollback(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    task.run();
                }
            }
        });
    }

    private Map<Long, List<ReportImage>> getReportImages(List<Report> reports) {
        List<Long> reportIds = reports.stream()
            .map(Report::getId)
            .toList();

        if (reportIds.isEmpty()) {
            return Map.of();
        }

        return reportImageRepository.findByReport_IdInOrderByReport_IdAscSortOrderAsc(reportIds)
            .stream()
            .collect(Collectors.groupingBy(reportImage -> reportImage.getReport().getId()));
    }

    private void validateAuthenticatedUser(Long userId) {
        if (userId == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED.getMessage(), ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateReportDraftRequest(ReportDraftRequest request) {
        validateCoordinate("위도", request.latitude(), MIN_LATITUDE, MAX_LATITUDE);
        validateCoordinate("경도", request.longitude(), MIN_LONGITUDE, MAX_LONGITUDE);
        validateReportImages(request.images(), false);
    }

    private void validateReportCreateRequest(ReportCreateRequest request, List<MultipartFile> images) {
        validateNotBlank("제보 제목", request.title());
        validateNotBlank("도로명 주소", request.roadAddress());
        validateNotBlank("지번 주소", request.jibunAddress());
        validateNotBlank("시/도", request.sido());
        validateNotBlank("시/군/구", request.sigungu());
        validateNotBlank("읍/면/동", request.eupmyeondong());
        if (request.contents() == null) {
            throw new CustomException("제보 본문은 필수입니다.", ErrorCode.INVALID_PARAMETER);
        }
        if (request.occurredAt() == null) {
            throw new CustomException("발생 시각은 필수입니다.", ErrorCode.INVALID_PARAMETER);
        }
        if (request.riskScore() == null) {
            throw new CustomException("위험 점수는 필수입니다.", ErrorCode.INVALID_PARAMETER);
        }
        if (request.visibility() == null) {
            throw new CustomException("공개 범위는 필수입니다.", ErrorCode.INVALID_PARAMETER);
        }
        if (request.categoryId() == null) {
            throw new CustomException("카테고리는 필수입니다.", ErrorCode.INVALID_PARAMETER);
        }
        if (request.departmentId() == null) {
            throw new CustomException("담당 부서는 필수입니다.", ErrorCode.INVALID_PARAMETER);
        }
        validateCoordinate("위도", request.latitude(), MIN_LATITUDE, MAX_LATITUDE);
        validateCoordinate("경도", request.longitude(), MIN_LONGITUDE, MAX_LONGITUDE);
        validateRiskScore(request.riskScore());
        validateEmbedding(request.embedding());
        if (!request.contents().isObject()) {
            throw new CustomException("제보 본문은 JSON 객체여야 합니다.", ErrorCode.INVALID_FORMAT);
        }
        validateReportImages(images, false);
    }

    private void validateNotBlank(String name, String value) {
        if (value == null || value.isBlank()) {
            throw new CustomException(name + "은 필수입니다.", ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateRiskScore(BigDecimal riskScore) {
        if (riskScore.compareTo(BigDecimal.ZERO) < 0 || riskScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new CustomException("위험 점수는 0부터 100 사이여야 합니다.", ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateEmbedding(List<BigDecimal> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return;
        }
        if (embedding.size() != REPORT_EMBEDDING_DIMENSION) {
            throw new CustomException("제보 임베딩은 1536차원이어야 합니다.", ErrorCode.INVALID_PARAMETER);
        }
        boolean hasNullValue = embedding.stream().anyMatch(value -> value == null);
        if (hasNullValue) {
            throw new CustomException("제보 임베딩 값은 null일 수 없습니다.", ErrorCode.INVALID_PARAMETER);
        }
    }

    private void validateReportImages(List<MultipartFile> images, boolean required) {
        if (images == null || images.isEmpty()) {
            if (required) {
                throw new CustomException("제보 이미지는 최소 1장 이상 첨부해야 합니다.", ErrorCode.MISSING_PARAMETER);
            }
            return;
        }
        if (images.size() > MAX_REPORT_DRAFT_IMAGE_COUNT) {
            throw new CustomException("제보 이미지는 최대 5장까지 첨부할 수 있습니다.", ErrorCode.INVALID_PARAMETER);
        }
        long validImageCount = images.stream()
            .filter(image -> image != null && !image.isEmpty())
            .count();
        if (required && validImageCount == 0) {
            throw new CustomException("제보 이미지는 최소 1장 이상 첨부해야 합니다.", ErrorCode.MISSING_PARAMETER);
        }
        images.stream()
            .filter(image -> image != null && !image.isEmpty())
            .forEach(image -> {
                if (image.getSize() > MAX_REPORT_DRAFT_IMAGE_SIZE) {
                    throw new CustomException("제보 이미지는 장당 50MB 이하만 첨부할 수 있습니다.", ErrorCode.INVALID_PARAMETER);
                }
                String contentType = image.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new CustomException("제보 이미지는 image/* 형식만 첨부할 수 있습니다.", ErrorCode.INVALID_FORMAT);
                }
            });
    }

    private void validateCoordinate(String name, BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) {
            throw new CustomException(name + "는 필수입니다.", ErrorCode.INVALID_PARAMETER);
        }
        if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
            throw new CustomException(
                name + "는 " + min.toPlainString() + "부터 " + max.toPlainString() + " 사이여야 합니다.",
                ErrorCode.INVALID_PARAMETER
            );
        }
    }

    private ReportCategory resolveReportCategory(ReportAiAnalyzeResponse aiResponse) {
        String categoryCode = aiResponse.category() == null ? null : aiResponse.category().categoryCode();
        if (categoryCode == null || categoryCode.isBlank()) {
            throw new CustomException("AI가 제보 카테고리를 분류하지 못했습니다.", ErrorCode.AI_SERVER_RESPONSE_ERROR);
        }

        return reportCategoryRepository.findByCategoryCode(categoryCode)
            .orElseThrow(() -> new CustomException("분류된 제보 카테고리를 찾을 수 없습니다.", ErrorCode.NOT_FOUND));
    }

    private ReportDraftResponse createReportDraftResponse(
        Long userId,
        ReportDraftRequest request,
        LocalDateTime occurredAt,
        AddressSnapshot address,
        ReportAiAnalyzeResponse aiResponse,
        ReportCategory category) {
        LocalDateTime resolvedOccurredAt = aiResponse.occurredAt() == null ? occurredAt : aiResponse.occurredAt();

        return new ReportDraftResponse(
            null,
            aiResponse.title(),
            resolveContents(aiResponse, request.content(), resolvedOccurredAt),
            request.latitude(),
            request.longitude(),
            address.roadAddress(),
            address.jibunAddress(),
            address.sido(),
            address.sigungu(),
            address.eupmyeondong(),
            resolvedOccurredAt,
            aiResponse.riskScore(),
            ReportStatus.SUBMITTED,
            false,
            ReportVisibility.PUBLIC,
            false,
            null,
            null,
            userId,
            category.getId(),
            null,
            category.getDepartment().getId(),
            aiResponse.embedding()
        );
    }

    private String toPgVector(List<BigDecimal> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return null;
        }

        return embedding.stream()
            .map(value -> String.format(Locale.ROOT, "%.10f", value))
            .collect(Collectors.joining(",", "[", "]"));
    }

    private JsonNode resolveContents(
        ReportAiAnalyzeResponse aiResponse,
        String originalContent,
        LocalDateTime occurredAt) {
        if (aiResponse.contents() != null && aiResponse.contents().isObject()) {
            return aiResponse.contents();
        }

        return JsonNodeFactory.instance.objectNode()
            .put("who", "확인되지 않음")
            .put("when", occurredAt.toString())
            .put("where", ADDRESS_NOT_RESOLVED)
            .put("what", originalContent)
            .put("how", originalContent)
            .put("why", "담당 기관 확인이 필요합니다.")
            .put("summary", originalContent);
    }

    private record MergeTarget(
        IssueGroup issueGroup,
        List<Report> groupReports,
        BigDecimal newGroupDiameter,
        BigDecimal score
    ) {
    }

    private record Coordinate(
        BigDecimal latitude,
        BigDecimal longitude
    ) {
    }

}
