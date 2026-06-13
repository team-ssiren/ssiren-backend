package com.ssaika.ssiren.global.util;

import com.ssaika.ssiren.domain.agency.entity.AgencyType;
import com.ssaika.ssiren.domain.agency.entity.Department;
import com.ssaika.ssiren.domain.agency.repository.AgencyTypeRepository;
import com.ssaika.ssiren.domain.agency.repository.DepartmentRepository;
import com.ssaika.ssiren.domain.chatbot.entity.ChatbotMessage;
import com.ssaika.ssiren.domain.chatbot.entity.ChatbotSession;
import com.ssaika.ssiren.domain.chatbot.repository.ChatbotMessageRepository;
import com.ssaika.ssiren.domain.chatbot.repository.ChatbotSessionRepository;
import com.ssaika.ssiren.domain.notification.entity.Notification;
import com.ssaika.ssiren.domain.notification.repository.NotificationRepository;
import com.ssaika.ssiren.domain.report.entity.*;
import com.ssaika.ssiren.domain.report.repository.*;
import com.ssaika.ssiren.domain.user.entity.OfficerDepartment;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.domain.user.entity.UserConsent;
import com.ssaika.ssiren.domain.user.entity.UserFcmToken;
import com.ssaika.ssiren.domain.user.repository.OfficerDepartmentRepository;
import com.ssaika.ssiren.domain.user.repository.UserConsentRepository;
import com.ssaika.ssiren.domain.user.repository.UserFcmTokenRepository;
import com.ssaika.ssiren.domain.user.repository.UserRepository;
import com.ssaika.ssiren.global.enums.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final AgencyTypeRepository agencyTypeRepository;
    private final DepartmentRepository departmentRepository;
    private final ReportCategoryRepository reportCategoryRepository;
    private final ReportCategoryMergeRuleRepository reportCategoryMergeRuleRepository;
    private final UserRepository userRepository;
    private final UserConsentRepository userConsentRepository;
    private final UserFcmTokenRepository userFcmTokenRepository;
    private final OfficerDepartmentRepository officerDepartmentRepository;
    private final IssueGroupRepository issueGroupRepository;
    private final ReportRepository reportRepository;
    private final ReportImageRepository reportImageRepository;
    private final ReportReactionLogRepository reportReactionLogRepository;
    private final ReportStatusHistoryRepository reportStatusHistoryRepository;
    private final ChatbotSessionRepository chatbotSessionRepository;
    private final ChatbotMessageRepository chatbotMessageRepository;
    private final NotificationRepository notificationRepository;

    // 더미 좌표/위험도 랜덤 배치용 (수지구 범위)
    private final Random random = new Random();

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        SeedTemplate template = saveTemplateDataIfNeeded();

        if (hasServiceData()) {
            return;
        }

        saveDummyData(template);
    }

    private boolean hasServiceData() {
        return userRepository.count() > 0
                || issueGroupRepository.count() > 0
                || reportRepository.count() > 0
                || chatbotSessionRepository.count() > 0
                || notificationRepository.count() > 0;
    }

    private SeedTemplate saveTemplateDataIfNeeded() {
        List<AgencyType> agencyTypes = agencyTypeRepository.count() == 0
                ? agencyTypeRepository.saveAll(List.of(
                        agencyType("수지구청"),
                        agencyType("용인서부경찰서"),
                        agencyType("용인서부소방서"),
                        agencyType("수지구보건소")))
                : findAllSorted(agencyTypeRepository);

        List<Department> departments = departmentRepository.count() == 0
                ? departmentRepository.saveAll(List.of(
                        // 수지구청 (지자체)
                        department("가정복지과", agencyTypes.get(0)),
                        department("건설도로과", agencyTypes.get(0)),
                        department("교통과", agencyTypes.get(0)),
                        department("도시건축과", agencyTypes.get(0)),
                        department("도시미관과", agencyTypes.get(0)),
                        department("동천동", agencyTypes.get(0)),
                        department("민원지적과", agencyTypes.get(0)),
                        department("사회복지과", agencyTypes.get(0)),
                        department("산업환경과", agencyTypes.get(0)),
                        department("상현1동", agencyTypes.get(0)),
                        department("상현2동", agencyTypes.get(0)),
                        department("상현3동", agencyTypes.get(0)),
                        department("성복동", agencyTypes.get(0)),
                        department("세무1과", agencyTypes.get(0)),
                        department("세무2과", agencyTypes.get(0)),
                        department("신봉동", agencyTypes.get(0)),
                        department("자치행정과", agencyTypes.get(0)),
                        department("죽전1동", agencyTypes.get(0)),
                        department("죽전2동", agencyTypes.get(0)),
                        department("죽전3동", agencyTypes.get(0)),
                        department("풍덕천1동", agencyTypes.get(0)),
                        department("풍덕천2동", agencyTypes.get(0)),
                        // 용인서부경찰서 (경찰)
                        department("경무과", agencyTypes.get(1)),
                        department("경비안보과", agencyTypes.get(1)),
                        department("교통과", agencyTypes.get(1)),
                        department("구성파출소", agencyTypes.get(1)),
                        department("범죄예방대응과", agencyTypes.get(1)),
                        department("보정지구대", agencyTypes.get(1)),
                        department("상현지구대", agencyTypes.get(1)),
                        department("수사과", agencyTypes.get(1)),
                        department("수지지구대", agencyTypes.get(1)),
                        department("신봉파출소", agencyTypes.get(1)),
                        department("여성청소년과", agencyTypes.get(1)),
                        department("죽전지구대", agencyTypes.get(1)),
                        department("청문감사인권관", agencyTypes.get(1)),
                        department("치안정보과", agencyTypes.get(1)),
                        department("형사과", agencyTypes.get(1)),
                        // 용인서부소방서 (소방)
                        department("119구조대", agencyTypes.get(2)),
                        department("구갈119안전센터", agencyTypes.get(2)),
                        department("기흥119안전센터", agencyTypes.get(2)),
                        department("동백119안전센터", agencyTypes.get(2)),
                        department("보정119안전센터", agencyTypes.get(2)),
                        department("성복119안전센터", agencyTypes.get(2)),
                        department("소방행정과", agencyTypes.get(2)),
                        department("수지119안전센터", agencyTypes.get(2)),
                        department("재난대응과", agencyTypes.get(2)),
                        department("청문인권담당관", agencyTypes.get(2)),
                        department("현장지휘단", agencyTypes.get(2)),
                        department("화재예방과", agencyTypes.get(2)),
                        // 수지구보건소 (보건)
                        department("건강증진과", agencyTypes.get(3)),
                        department("보건행정과", agencyTypes.get(3)),
                        department("치매안심센터팀", agencyTypes.get(3))))
                : findAllSorted(departmentRepository);

        List<ReportCategory> reportCategories = reportCategoryRepository.count() == 0
                ? saveReportCategoryTemplate()
                : findAllSorted(reportCategoryRepository);
        reportCategories = saveInsufficientCategoryIfNeeded(reportCategories);

        saveReportCategoryMergeRulesIfNeeded(reportCategories);

        return new SeedTemplate(departments, reportCategories);
    }

    private List<ReportCategory> saveReportCategoryTemplate() {
        List<ReportCategory> parents = reportCategoryRepository.saveAll(List.of(
                reportCategory("TRAFFIC", "교통", null),
                reportCategory("INFRASTRUCTURE_ROAD", "시설물", null),
                reportCategory("LIVING_INCONVENIENCE", "생활불편", null),
                reportCategory("LIFE_SAFETY", "생활안전", null),
                reportCategory("CONSTRUCTION_SITE", "공사장", null),
                reportCategory("PUBLIC_ORDER", "치안", null),
                reportCategory("PUBLIC_HEALTH_WELFARE", "보건복지", null),
                reportCategory("ETC", "기타", null)));

        List<ReportCategory> children = reportCategoryRepository.saveAll(List.of(
                reportCategory("ILLEGAL_PARKING", "불법주정차", parents.get(0)),
                reportCategory("TRAFFIC_VIOLATION", "교통위반", parents.get(0)),
                reportCategory("ABANDONED_VEHICLE", "방치차량", parents.get(0)),
                reportCategory("PARKING_LOT_ISSUE", "주차장 불편", parents.get(0)),
                reportCategory("PUBLIC_TRANSPORT_ISSUE", "대중교통 운행 불편", parents.get(0)),
                reportCategory("ROAD_DAMAGE", "도로 파손", parents.get(1)),
                reportCategory("ROAD_FACILITY_DAMAGE", "도로시설 파손", parents.get(1)),
                reportCategory("SIDEWALK_DAMAGE", "보도블록 파손", parents.get(1)),
                reportCategory("STREETLIGHT_FAILURE", "가로등 고장", parents.get(1)),
                reportCategory("TRAFFIC_FACILITY_FAILURE", "교통시설물 고장", parents.get(1)),
                reportCategory("MANHOLE_DRAIN_DAMAGE", "맨홀·배수구 파손", parents.get(1)),
                reportCategory("PUBLIC_FACILITY_DAMAGE", "공공시설물 파손", parents.get(1)),
                reportCategory("PUBLIC_USE_FACILITY_SAFETY", "다중이용시설 안전 문제", parents.get(1)),
                reportCategory("OBSTRUCTION_ACCESS_BLOCKAGE", "장애물·통행 방해", parents.get(1)),
                reportCategory("AGING_FACILITY_RISK", "노후 시설물 위험", parents.get(1)),
                reportCategory("PARK_FACILITY_DAMAGE", "공원시설 파손", parents.get(1)),
                reportCategory("WASTE_AND_DEBRIS", "쓰레기·폐기물", parents.get(2)),
                reportCategory("ILLEGAL_ADVERTISEMENT", "불법광고물", parents.get(2)),
                reportCategory("ODOR", "악취", parents.get(2)),
                reportCategory("NOISE", "소음", parents.get(2)),
                reportCategory("AIR_POLLUTION_DUST", "대기오염·비산먼지", parents.get(2)),
                reportCategory("WATER_POLLUTION_WASTEWATER", "수질오염·오폐수", parents.get(2)),
                reportCategory("ILLEGAL_BURNING", "불법소각", parents.get(2)),
                reportCategory("LIGHT_POLLUTION", "빛공해", parents.get(2)),
                reportCategory("PET_NUISANCE", "반려동물 불편", parents.get(2)),
                reportCategory("FLOODING_RISK", "침수 위험", parents.get(3)),
                reportCategory("SEWER_BACKFLOW", "하수도 역류", parents.get(3)),
                reportCategory("RIVER_FACILITY_RISK", "하천 위험", parents.get(3)),
                reportCategory("BEEHIVE_RISK", "벌집 위험", parents.get(3)),
                reportCategory("STRAY_OR_DANGEROUS_ANIMAL", "유기동물·위험동물", parents.get(3)),
                reportCategory("FIRE_RISK", "화재위험", parents.get(3)),
                reportCategory("GAS_ELECTRIC_RISK", "가스·전기 위험", parents.get(3)),
                reportCategory("CONSTRUCTION_SAFETY_VIOLATION", "공사장 안전조치 미흡", parents.get(4)),
                reportCategory("CONSTRUCTION_NOISE", "공사장 소음", parents.get(4)),
                reportCategory("CONSTRUCTION_CRACK_DAMAGE", "공사로 인한 균열", parents.get(4)),
                reportCategory("CONSTRUCTION_ACCESS_BLOCKAGE", "공사장 통행 불편", parents.get(4)),
                reportCategory("INTOXICATED_PERSON_CONCERN", "취객·주취자 불안", parents.get(5)),
                reportCategory("DISORDERLY_CONDUCT_DISPUTE", "행패소란·시비", parents.get(5)),
                reportCategory("SUSPICIOUS_ACTIVITY", "범죄의심·방범불안", parents.get(5)),
                reportCategory("ILLEGAL_FILMING_SUSPICION", "불법촬영 의심", parents.get(5)),
                reportCategory("YOUTH_DELINQUENCY_DISTURBANCE", "청소년 비행·집단소란", parents.get(5)),
                reportCategory("ACCESSIBILITY_FACILITY_ISSUE", "장애인 편의시설 불편", parents.get(6)),
                reportCategory("PUBLIC_HYGIENE_ISSUE", "공중위생 불량", parents.get(6)),
                reportCategory("FOOD_HYGIENE_REPORT", "식품위생 신고", parents.get(6)),
                reportCategory("PEST_CONTROL_ISSUE", "해충 문제", parents.get(6)),
                reportCategory("VULNERABLE_PERSON_RISK", "노약자 위험 상황", parents.get(6)),
                reportCategory("YOUTH_RISK_ENVIRONMENT", "청소년 위험 환경", parents.get(6)),
                reportCategory("ETC_OTHER", "기타", parents.get(7)),
                reportCategory("INSUFFICIENT", "제보 불성립", parents.get(7))));

        List<ReportCategory> categories = new ArrayList<>(parents);
        categories.addAll(children);
        return categories;
    }

    private List<ReportCategory> saveInsufficientCategoryIfNeeded(List<ReportCategory> reportCategories) {
        boolean exists = reportCategories.stream()
                .anyMatch(category -> "INSUFFICIENT".equals(category.getCategoryCode()));
        if (exists) {
            return reportCategories;
        }

        ReportCategory parent = reportCategories.stream()
                .filter(category -> "ETC".equals(category.getCategoryCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seed category not found: ETC"));
        ReportCategory insufficient = reportCategoryRepository.save(
                reportCategory("INSUFFICIENT", "제보 불성립", parent));

        List<ReportCategory> categories = new ArrayList<>(reportCategories);
        categories.add(insufficient);
        categories.sort(Comparator.comparing(ReportCategory::getId));
        return categories;
    }

    private void saveReportCategoryMergeRulesIfNeeded(List<ReportCategory> reportCategories) {
        if (reportCategoryMergeRuleRepository.count() > 0) {
            return;
        }

        SeedTemplate template = new SeedTemplate(List.of(), reportCategories);
        reportCategoryMergeRuleRepository.saveAll(List.of(
                mergeRule(template.categoryByCode("ILLEGAL_PARKING"), 30, 100, "0.80", "80"),
                mergeRule(template.categoryByCode("TRAFFIC_VIOLATION"), 50, 150, "0.80", "82"),
                mergeRule(template.categoryByCode("ABANDONED_VEHICLE"), 50, 150, "0.78", "80"),
                mergeRule(template.categoryByCode("PARKING_LOT_ISSUE"), 80, 200, "0.76", "82"),
                mergeRule(template.categoryByCode("PUBLIC_TRANSPORT_ISSUE"), 150, 500, "0.74", "85"),
                mergeRule(template.categoryByCode("ROAD_DAMAGE"), 50, 120, "0.78", "80"),
                mergeRule(template.categoryByCode("ROAD_FACILITY_DAMAGE"), 50, 150, "0.78", "80"),
                mergeRule(template.categoryByCode("SIDEWALK_DAMAGE"), 40, 100, "0.80", "80"),
                mergeRule(template.categoryByCode("STREETLIGHT_FAILURE"), 20, 50, "0.82", "80"),
                mergeRule(template.categoryByCode("TRAFFIC_FACILITY_FAILURE"), 30, 80, "0.80", "80"),
                mergeRule(template.categoryByCode("MANHOLE_DRAIN_DAMAGE"), 30, 80, "0.80", "80"),
                mergeRule(template.categoryByCode("PUBLIC_FACILITY_DAMAGE"), 50, 150, "0.78", "80"),
                mergeRule(template.categoryByCode("PUBLIC_USE_FACILITY_SAFETY"), 60, 200, "0.77", "82"),
                mergeRule(template.categoryByCode("OBSTRUCTION_ACCESS_BLOCKAGE"), 50, 120, "0.78", "80"),
                mergeRule(template.categoryByCode("AGING_FACILITY_RISK"), 80, 250, "0.76", "82"),
                mergeRule(template.categoryByCode("PARK_FACILITY_DAMAGE"), 60, 180, "0.78", "80"),
                mergeRule(template.categoryByCode("WASTE_AND_DEBRIS"), 50, 120, "0.78", "80"),
                mergeRule(template.categoryByCode("ILLEGAL_ADVERTISEMENT"), 30, 100, "0.80", "80"),
                mergeRule(template.categoryByCode("ODOR"), 150, 400, "0.75", "82"),
                mergeRule(template.categoryByCode("NOISE"), 150, 400, "0.75", "82"),
                mergeRule(template.categoryByCode("AIR_POLLUTION_DUST"), 200, 600, "0.73", "84"),
                mergeRule(template.categoryByCode("WATER_POLLUTION_WASTEWATER"), 150, 500, "0.74", "84"),
                mergeRule(template.categoryByCode("ILLEGAL_BURNING"), 100, 300, "0.76", "84"),
                mergeRule(template.categoryByCode("LIGHT_POLLUTION"), 150, 400, "0.75", "82"),
                mergeRule(template.categoryByCode("PET_NUISANCE"), 100, 250, "0.76", "82"),
                mergeRule(template.categoryByCode("FLOODING_RISK"), 150, 500, "0.72", "84"),
                mergeRule(template.categoryByCode("SEWER_BACKFLOW"), 50, 150, "0.78", "82"),
                mergeRule(template.categoryByCode("RIVER_FACILITY_RISK"), 150, 500, "0.74", "84"),
                mergeRule(template.categoryByCode("BEEHIVE_RISK"), 30, 80, "0.82", "80"),
                mergeRule(template.categoryByCode("STRAY_OR_DANGEROUS_ANIMAL"), 30, 80, "0.80", "80"),
                mergeRule(template.categoryByCode("FIRE_RISK"), 150, 500, "0.70", "85"),
                mergeRule(template.categoryByCode("GAS_ELECTRIC_RISK"), 80, 250, "0.76", "85"),
                mergeRule(template.categoryByCode("CONSTRUCTION_SAFETY_VIOLATION"), 80, 250, "0.76", "82"),
                mergeRule(template.categoryByCode("CONSTRUCTION_NOISE"), 150, 400, "0.75", "82"),
                mergeRule(template.categoryByCode("CONSTRUCTION_CRACK_DAMAGE"), 80, 250, "0.77", "82"),
                mergeRule(template.categoryByCode("CONSTRUCTION_ACCESS_BLOCKAGE"), 50, 150, "0.78", "80"),
                mergeRule(template.categoryByCode("INTOXICATED_PERSON_CONCERN"), 80, 200, "0.78", "82"),
                mergeRule(template.categoryByCode("DISORDERLY_CONDUCT_DISPUTE"), 80, 200, "0.78", "82"),
                mergeRule(template.categoryByCode("SUSPICIOUS_ACTIVITY"), 100, 250, "0.76", "82"),
                mergeRule(template.categoryByCode("ILLEGAL_FILMING_SUSPICION"), 50, 150, "0.82", "88"),
                mergeRule(template.categoryByCode("YOUTH_DELINQUENCY_DISTURBANCE"), 100, 250, "0.76", "82"),
                mergeRule(template.categoryByCode("ACCESSIBILITY_FACILITY_ISSUE"), 50, 150, "0.78", "82"),
                mergeRule(template.categoryByCode("PUBLIC_HYGIENE_ISSUE"), 80, 250, "0.76", "82"),
                mergeRule(template.categoryByCode("FOOD_HYGIENE_REPORT"), 30, 100, "0.80", "85"),
                mergeRule(template.categoryByCode("PEST_CONTROL_ISSUE"), 80, 250, "0.76", "82"),
                mergeRule(template.categoryByCode("VULNERABLE_PERSON_RISK"), 100, 300, "0.75", "82"),
                mergeRule(template.categoryByCode("YOUTH_RISK_ENVIRONMENT"), 100, 250, "0.76", "82"),
                mergeRule(template.categoryByCode("ETC_OTHER"), 30, 80, "0.85", "90")));
    }

    private void saveDummyData(SeedTemplate template) {
        List<User> citizens = userRepository.saveAll(List.of(
                user("minji.citizen@ssiren.kr", "역삼동민지", UserRole.CITIZEN, true),
                user("hyunwoo.citizen@ssiren.kr", "출근길현우", UserRole.CITIZEN, true),
                user("yujin.citizen@ssiren.kr", "강남산책유진", UserRole.CITIZEN, false)));
        List<User> operators = userRepository.saveAll(List.of(
                user("seoha.officer@ssiren.kr", "강남구청서하", UserRole.OFFICER, true),
                user("jiho.officer@ssiren.kr", "도시안전지호", UserRole.OFFICER, true),
                user("admin@ssiren.kr", "씨렌관리자", UserRole.ADMIN, true)));

        List<User> users = new ArrayList<>(citizens);
        users.addAll(operators);

        saveUserChildren(users, operators, template.departments());

        List<IssueGroup> issueGroups = issueGroupRepository.saveAll(List.of(
                issueGroup("수지구 보도블록 파손 구간", "출근 시간대 보행자가 반복적으로 불편을 겪는 구간입니다.", "ROAD_DAMAGE"),
                issueGroup("수지구 골목 쓰레기 무단투기", "상가 뒤편에 생활 폐기물과 음식물 쓰레기가 반복 적치됩니다.", "WASTE_AND_DEBRIS"),
                issueGroup("수지구 어린이보호구역 불법주정차", "등교 시간대 횡단보도 시야가 가려지는 상황입니다.", "ILLEGAL_PARKING")));
        List<FixedIssueGroupSeed> fixedIssueGroupSeeds = fixedIssueGroupSeeds();
        List<FixedReportSeed> fixedReportSeeds = fixedReportSeeds();
        issueGroups = new ArrayList<>(issueGroups);
        issueGroups.addAll(issueGroupRepository.saveAll(
                fixedIssueGroupSeeds.stream()
                        .map(seed -> issueGroup(seed.title(), seed.content(), seed.latitude(), seed.longitude(), seed.riskScore(), seed.reportCount()))
                        .toList()));

        List<Report> reports = reportRepository.saveAll(createReports(citizens, issueGroups, template));
        reports = new ArrayList<>(reports);
        reports.addAll(reportRepository.saveAll(createFixedReports(citizens, issueGroups, template, fixedReportSeeds, 3)));

        saveReportChildren(reports, users, operators.get(0));
        saveChatbotData(users);
        saveNotifications(users, reports);
    }

    private List<Report> createReports(List<User> citizens, List<IssueGroup> issueGroups, SeedTemplate template) {
        return List.of(
                report("보도블록이 내려앉아 발목을 접질릴 뻔했습니다.", "보도블록 파손", "경기도 용인시 수지구 풍덕천로 152", "경기도 용인시 수지구 풍덕천동 737", citizens.get(0), template.categoryByCode("ROAD_DAMAGE"), template.departmentByName("수지구청", "건설도로과"), issueGroups.get(0), ReportStatus.SUBMITTED, true),
                report("비 오는 날마다 같은 보도블록 구간에 물이 고입니다.", "침하된 보도블록", "경기도 용인시 수지구 풍덕천로 156", "경기도 용인시 수지구 풍덕천동 738", citizens.get(1), template.categoryByCode("ROAD_DAMAGE"), template.departmentByName("수지구청", "건설도로과"), issueGroups.get(0), ReportStatus.CHECKING, false),
                report("파손 구간 옆 안내 표시가 없어 야간에 위험합니다.", "보행 안전 표시 필요", "경기도 용인시 수지구 풍덕천로 150", "경기도 용인시 수지구 풍덕천동 736", citizens.get(2), template.categoryByCode("ROAD_DAMAGE"), template.departmentByName("수지구청", "건설도로과"), issueGroups.get(0), ReportStatus.IN_PROGRESS, false),
                report("퇴근길마다 같은 위치에 생활 쓰레기가 쌓입니다.", "쓰레기 무단투기", "경기도 용인시 수지구 죽전로 513", "경기도 용인시 수지구 죽전동 159", citizens.get(0), template.categoryByCode("WASTE_AND_DEBRIS"), template.departmentByName("수지구청", "산업환경과"), issueGroups.get(1), ReportStatus.RECEIVED, true),
                report("상가 뒤편에 종량제 봉투가 아닌 폐기물이 방치되어 있습니다.", "폐기물 방치", "경기도 용인시 수지구 죽전로 517", "경기도 용인시 수지구 죽전동 160", citizens.get(1), template.categoryByCode("WASTE_AND_DEBRIS"), template.departmentByName("수지구청", "산업환경과"), issueGroups.get(1), ReportStatus.SUBMITTED, false),
                report("음식물 쓰레기 냄새가 골목 전체로 퍼지고 있습니다.", "음식물 쓰레기 적치", "경기도 용인시 수지구 죽전로 519", "경기도 용인시 수지구 죽전동 161", citizens.get(2), template.categoryByCode("WASTE_AND_DEBRIS"), template.departmentByName("수지구청", "산업환경과"), issueGroups.get(1), ReportStatus.IN_PROGRESS, false),
                report("어린이보호구역 횡단보도 앞 불법주정차입니다.", "불법주정차", "경기도 용인시 수지구 상현로 120", "경기도 용인시 수지구 상현동 142", citizens.get(0), template.categoryByCode("ILLEGAL_PARKING"), template.departmentByName("수지구청", "교통과"), issueGroups.get(2), ReportStatus.CHECKING, true),
                report("등교 시간에 횡단보도 모퉁이를 차량이 막고 있습니다.", "보호구역 시야 방해", "경기도 용인시 수지구 상현로 124", "경기도 용인시 수지구 상현동 143", citizens.get(1), template.categoryByCode("ILLEGAL_PARKING"), template.departmentByName("수지구청", "교통과"), issueGroups.get(2), ReportStatus.RECEIVED, false),
                report("어린이집 앞 정차 차량으로 보행 동선이 막힙니다.", "어린이집 앞 정차", "경기도 용인시 수지구 상현로 118", "경기도 용인시 수지구 상현동 141", citizens.get(2), template.categoryByCode("ILLEGAL_PARKING"), template.departmentByName("수지구청", "교통과"), issueGroups.get(2), ReportStatus.SUBMITTED, false));
    }

    private List<Report> createFixedReports(
            List<User> citizens,
            List<IssueGroup> issueGroups,
            SeedTemplate template,
            List<FixedReportSeed> seeds,
            int issueGroupStartIndex) {
        List<Report> reports = new ArrayList<>();
        for (int index = 0; index < seeds.size(); index++) {
            FixedReportSeed seed = seeds.get(index);
            reports.add(report(
                    seed.title(),
                    seed.summary(),
                    seed.latitude(),
                    seed.longitude(),
                    seed.roadAddress(),
                    seed.jibunAddress(),
                    seed.riskScore(),
                    citizens.get(index % citizens.size()),
                    template.categoryByCode(seed.categoryCode()),
                    template.departmentByName(seed.agencyName(), seed.departmentName()),
                    issueGroups.get(issueGroupStartIndex + seed.issueGroupIndex()),
                    seed.status(),
                    true));
        }
        return reports;
    }

    private List<FixedIssueGroupSeed> fixedIssueGroupSeeds() {
        return List.of(
                fixedIssueGroupSeed("죽전동 상가 골목 불법주정차 반복", "상가 진입로와 보행로 주변에 차량이 반복적으로 정차해 차량 교행과 보행 안전에 지장이 있습니다.", "37.3532878", "127.0703683", "30.00", 5),
                fixedIssueGroupSeed("죽전동 편의점 앞 주취자 불안 신고", "편의점과 벤치 주변에서 주취자가 머물며 고성, 출입 방해, 보행자 접촉 시도가 반복되고 있습니다.", "37.3533378", "127.0703883", "60.00", 3),
                fixedIssueGroupSeed("동편 상가 앞 고성 및 시비", "상가 앞 인도에서 고성과 시비가 이어져 주변 보행자가 우회하고 현장 확인 요청이 반복됩니다.", "37.3533676", "127.0725919", "80.00", 3),
                fixedIssueGroupSeed("남서쪽 골목 생활폐기물 무단투기", "골목 입구와 상가 뒤편에 종량제 외 폐기물과 음식물 쓰레기가 반복적으로 쌓이고 있습니다.", "37.3514273", "127.0702762", "30.00", 4),
                fixedIssueGroupSeed("남서쪽 이면도로 불법주정차 반복", "이면도로와 교차로 주변에 차량이 연속 정차해 통행 폭이 좁아지고 진입이 어려운 상황이 반복됩니다.", "37.3514273", "127.0702762", "30.00", 10));
    }

    private FixedIssueGroupSeed fixedIssueGroupSeed(
            String title,
            String content,
            String latitude,
            String longitude,
            String riskScore,
            int reportCount) {
        return new FixedIssueGroupSeed(title, content, latitude, longitude, riskScore, reportCount);
    }
    private List<FixedReportSeed> fixedReportSeeds() {
        return List.of(
                fixedReportSeed(0, "골목 입구에 세워진 차량 때문에 차가 한 대씩만 지나갑니다.", "골목 입구 불법주정차", "37.3532878", "127.0703683", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.SUBMITTED),
                fixedReportSeed(0, "상가 모퉁이에 장시간 정차한 차량 때문에 우회전 차량 시야가 가립니다.", "모퉁이 장시간 정차", "37.3534078", "127.0704583", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.RECEIVED),
                fixedReportSeed(0, "보행로 가장자리까지 차량이 올라와 유모차가 차도로 돌아가고 있습니다.", "보행로 침범 주차", "37.3531878", "127.0705083", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.SUBMITTED),
                fixedReportSeed(0, "건물 출입구 앞 정차 차량 때문에 사람과 배달 오토바이가 뒤섞입니다.", "출입구 앞 정차", "37.3534478", "127.0702483", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.CHECKING),
                fixedReportSeed(0, "소방 통로 표시선 안쪽에 차량이 계속 세워져 있습니다.", "소방 통로 주차", "37.3531478", "127.0702883", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.SUBMITTED),
                fixedReportSeed(1, "편의점 앞 벤치에서 술에 취한 사람이 계속 소리를 지르고 있습니다.", "편의점 앞 주취자 고성", "37.3535078", "127.0703883", "60.00", "INTOXICATED_PERSON_CONCERN", "용인서부경찰서", "범죄예방대응과", ReportStatus.RECEIVED),
                fixedReportSeed(1, "주취자가 지나가는 사람에게 말을 걸며 따라붙어 불안합니다.", "보행자 접촉 시도", "37.3530878", "127.0705683", "60.00", "INTOXICATED_PERSON_CONCERN", "용인서부경찰서", "범죄예방대응과", ReportStatus.CHECKING),
                fixedReportSeed(1, "출입문 근처에 취객이 앉아 있어 손님들이 들어가기를 꺼립니다.", "출입 방해 주취자", "37.3533378", "127.0701283", "60.00", "INTOXICATED_PERSON_CONCERN", "용인서부경찰서", "범죄예방대응과", ReportStatus.SUBMITTED),
                fixedReportSeed(2, "상가 앞에서 두 사람이 큰 소리로 다투고 있어 보행자가 피하고 있습니다.", "상가 앞 고성 시비", "37.3533676", "127.0725919", "80.00", "DISORDERLY_CONDUCT_DISPUTE", "용인서부경찰서", "범죄예방대응과", ReportStatus.RECEIVED),
                fixedReportSeed(2, "몸싸움으로 번질 것처럼 서로 밀치고 있어 현장 확인이 필요합니다.", "몸싸움 우려 시비", "37.3535076", "127.0725019", "80.00", "DISORDERLY_CONDUCT_DISPUTE", "용인서부경찰서", "범죄예방대응과", ReportStatus.CHECKING),
                fixedReportSeed(2, "인도 한가운데에서 고성이 오가며 사람들이 차도로 돌아가고 있습니다.", "인도 위 고성 다툼", "37.3532476", "127.0727219", "80.00", "DISORDERLY_CONDUCT_DISPUTE", "용인서부경찰서", "범죄예방대응과", ReportStatus.SUBMITTED),
                fixedReportSeed(3, "골목 입구에 종량제 봉투가 아닌 생활폐기물이 쌓여 있습니다.", "생활폐기물 무단 적치", "37.3514273", "127.0702762", "30.00", "WASTE_AND_DEBRIS", "수지구청", "산업환경과", ReportStatus.SUBMITTED),
                fixedReportSeed(3, "상가 뒤편에 박스와 비닐류가 며칠째 치워지지 않고 있습니다.", "상가 뒤편 폐기물", "37.3515573", "127.0704162", "30.00", "WASTE_AND_DEBRIS", "수지구청", "산업환경과", ReportStatus.RECEIVED),
                fixedReportSeed(3, "음식물 쓰레기 봉투가 터져 냄새가 골목까지 퍼집니다.", "음식물 쓰레기 방치", "37.3512873", "127.0703962", "30.00", "WASTE_AND_DEBRIS", "수지구청", "산업환경과", ReportStatus.SUBMITTED),
                fixedReportSeed(3, "분리수거장 밖에 폐기물이 계속 놓여 통행 공간이 줄었습니다.", "분리수거장 밖 적치", "37.3515073", "127.0700862", "30.00", "WASTE_AND_DEBRIS", "수지구청", "산업환경과", ReportStatus.CHECKING),
                fixedReportSeed(4, "이면도로 한쪽을 차량이 막아 마주 오는 차가 후진해야 합니다.", "이면도로 통행 방해", "37.3514273", "127.0702762", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.SUBMITTED),
                fixedReportSeed(4, "횡단 지점 가까이에 정차한 차량 때문에 보행자 확인이 어렵습니다.", "횡단 지점 정차", "37.3516073", "127.0702762", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.RECEIVED),
                fixedReportSeed(4, "주차장 출입로 앞에 차량이 서 있어 진입 차량이 대기합니다.", "주차장 출입로 막힘", "37.3512473", "127.0702762", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.SUBMITTED),
                fixedReportSeed(4, "보도 가장자리에 걸쳐 세운 차량 때문에 보행자가 차도로 내려갑니다.", "보도 걸침 주차", "37.3514273", "127.0704862", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.CHECKING),
                fixedReportSeed(4, "교차로 모퉁이에 세워진 차량 때문에 회전 차량이 크게 돌아갑니다.", "교차로 모퉁이 주차", "37.3514273", "127.0700662", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.SUBMITTED),
                fixedReportSeed(4, "차량 두 대가 연속으로 정차해 차로 폭이 거의 남지 않았습니다.", "연속 정차 차량", "37.3515573", "127.0701262", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.RECEIVED),
                fixedReportSeed(4, "배송 차량이 장시간 정차해 뒤 차량들이 중앙선을 넘어갑니다.", "배송 차량 장시간 정차", "37.3512973", "127.0704262", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.SUBMITTED),
                fixedReportSeed(4, "소화전 주변에 차량이 세워져 긴급 상황이 걱정됩니다.", "소화전 주변 주차", "37.3516273", "127.0704862", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.CHECKING),
                fixedReportSeed(4, "골목 입구 정차 차량 때문에 택배차가 들어오지 못하고 있습니다.", "골목 입구 정차", "37.3512173", "127.0700562", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.RECEIVED),
                fixedReportSeed(4, "주정차 금지 표지 아래 같은 차량이 반복적으로 서 있습니다.", "금지구역 반복 주차", "37.3516773", "127.0702462", "30.00", "ILLEGAL_PARKING", "수지구청", "교통과", ReportStatus.SUBMITTED));
    }

    private FixedReportSeed fixedReportSeed(
            int issueGroupIndex,
            String title,
            String summary,
            String latitude,
            String longitude,
            String riskScore,
            String categoryCode,
            String agencyName,
            String departmentName,
            ReportStatus status) {
        return new FixedReportSeed(
                issueGroupIndex,
                title,
                summary,
                latitude,
                longitude,
                "경기도 용인시 수지구 죽전동 일대",
                "경기도 용인시 수지구 죽전동 일대",
                riskScore,
                categoryCode,
                agencyName,
                departmentName,
                status);
    }

    private void saveUserChildren(List<User> users, List<User> operators, List<Department> departments) {
        List<UserConsent> consents = new ArrayList<>();
        List<UserFcmToken> fcmTokens = new ArrayList<>();
        for (User user : users) {
            for (int index = 1; index <= 3; index++) {
                consents.add(userConsent(user, true, index != 2));
                fcmTokens.add(userFcmToken(user, "ssiren-fcm-" + user.getNickname() + "-" + index, index != 3));
            }
        }

        List<OfficerDepartment> officerDepartments = new ArrayList<>();
        for (User operator : operators) {
            officerDepartments.add(officerDepartment(operator, departmentByName(departments, "수지구청", "건설도로과")));
            officerDepartments.add(officerDepartment(operator, departmentByName(departments, "수지구청", "산업환경과")));
            officerDepartments.add(officerDepartment(operator, departmentByName(departments, "수지구청", "교통과")));
        }

        userConsentRepository.saveAll(consents);
        userFcmTokenRepository.saveAll(fcmTokens);
        officerDepartmentRepository.saveAll(officerDepartments);
    }

    private void saveReportChildren(List<Report> reports, List<User> users, User officer) {
        List<ReportImage> images = new ArrayList<>();
        List<ReportReactionLog> reactions = new ArrayList<>();
        List<ReportStatusHistory> histories = new ArrayList<>();

        for (int reportIndex = 0; reportIndex < reports.size(); reportIndex++) {
            Report report = reports.get(reportIndex);
            for (int index = 1; index <= 3; index++) {
                images.add(reportImage(report, "https://cdn.ssiren.kr/reports/" + (reportIndex + 1) + "/image-" + index + ".jpg", index));
                reactions.add(reportReactionLog(report, users.get((reportIndex + index) % users.size()), ReportReactionType.values()[(index - 1) % ReportReactionType.values().length]));
                histories.add(reportStatusHistory(report, officer, index == 1 ? null : ReportStatus.values()[index - 2], ReportStatus.values()[index - 1], statusReason(index)));
            }
        }

        reportImageRepository.saveAll(images);
        reportReactionLogRepository.saveAll(reactions);
        reportStatusHistoryRepository.saveAll(histories);
    }

    private void saveChatbotData(List<User> users) {
        List<ChatbotSession> sessions = new ArrayList<>();
        for (User user : users) {
            sessions.add(chatbotSession(user, "신고 카테고리 분류 상담"));
            sessions.add(chatbotSession(user, "위치 기반 주변 신고 확인"));
            sessions.add(chatbotSession(user, "처리 상태 알림 문의"));
        }
        sessions = chatbotSessionRepository.saveAll(sessions);

        List<ChatbotMessage> messages = new ArrayList<>();
        for (ChatbotSession session : sessions) {
            messages.add(chatbotMessage(session, ChatbotSenderType.USER, "사진과 위치가 있는데 어떤 민원으로 신고하면 좋을까요?"));
            messages.add(chatbotMessage(session, ChatbotSenderType.BOT, "위치, 사진, 발생 시간을 함께 등록하면 담당 부서 배정 정확도가 높아집니다."));
            messages.add(chatbotMessage(session, ChatbotSenderType.USER, "비슷한 신고가 있으면 함께 묶여 처리되나요?"));
        }
        chatbotMessageRepository.saveAll(messages);
    }

    private void saveNotifications(List<User> users, List<Report> reports) {
        List<Notification> notifications = new ArrayList<>();
        for (int reportIndex = 0; reportIndex < reports.size(); reportIndex++) {
            Report report = reports.get(reportIndex);
            for (int index = 0; index < 3; index++) {
                notifications.add(notification(
                        users.get((reportIndex + index) % users.size()),
                        report,
                        report.getIssueGroup(),
                        NotificationType.values()[index],
                        index == 2));
            }
        }
        notificationRepository.saveAll(notifications);
    }

    private AgencyType agencyType(String name) {
        return entity(AgencyType.class)
                .set("name", name)
                .get();
    }

    private Department department(String name, AgencyType agencyType) {
        return entity(Department.class)
                .set("name", name)
                .set("agencyType", agencyType)
                .get();
    }

    private Department departmentByName(List<Department> departments, String agencyName, String departmentName) {
        return departments.stream()
                .filter(department -> departmentName.equals(department.getName()))
                .filter(department -> agencyName.equals(department.getAgencyType().getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Seed department not found: " + agencyName + " / " + departmentName));
    }

    private ReportCategory reportCategory(String code, String name, ReportCategory parent) {
        return entity(ReportCategory.class)
                .set("categoryCode", code)
                .set("categoryName", name)
                .set("parentCategory", parent)
                .get();
    }

    private User user(String email, String nickname, UserRole role, boolean alarmEnabled) {
        User user = User.createKakaoUser(email, nickname);
        return entity(user)
                .set("role", role)
                .set("roleSelected", true)
                .set("isAlarmEnabled", alarmEnabled)
                .get();
    }

    private UserConsent userConsent(User user, boolean locationAgreed, boolean sensitiveInfoAgreed) {
        return entity(UserConsent.class)
                .set("locationAgreed", locationAgreed)
                .set("sensitiveInfoAgreed", sensitiveInfoAgreed)
                .set("sensitiveInfoAgreedAt", sensitiveInfoAgreed ? LocalDateTime.now().minusDays(7) : null)
                .set("user", user)
                .get();
    }

    private UserFcmToken userFcmToken(User user, String token, boolean active) {
        return entity(UserFcmToken.class)
                .set("fcmToken", token)
                .set("isActive", active)
                .set("user", user)
                .get();
    }

    private OfficerDepartment officerDepartment(User user, Department department) {
        return entity(OfficerDepartment.class)
                .set("user", user)
                .set("department", department)
                .get();
    }

    private IssueGroup issueGroup(String title, String content, String categoryCode) {
        return entity(IssueGroup.class)
                .set("title", title)
                .set("content", content)
                .set("groupLatitude", randomLatitude())
                .set("groupLongitude", randomLongitude())
                .set("reportCount", 3)
                .set("yesCount", 2)
                .set("noCount", 0)
                .set("unknownCount", 1)
                .set("recentReportedAt", LocalDateTime.now().minusHours(2))
                .set("status", IssueGroupStatus.ACTIVE)
                .set("riskScore", riskScoreFor(categoryCode))
                .set("groupDiameterMeters", BigDecimal.ZERO)
                .get();
    }

    private IssueGroup issueGroup(String title, String content, String latitude, String longitude, String riskScore, int reportCount) {
        return entity(IssueGroup.class)
                .set("title", title)
                .set("content", content)
                .set("groupLatitude", new BigDecimal(latitude))
                .set("groupLongitude", new BigDecimal(longitude))
                .set("reportCount", reportCount)
                .set("yesCount", Math.min(reportCount, 2))
                .set("noCount", 0)
                .set("unknownCount", Math.max(reportCount - 2, 0))
                .set("recentReportedAt", LocalDateTime.now().minusHours(1))
                .set("status", IssueGroupStatus.ACTIVE)
                .set("riskScore", new BigDecimal(riskScore))
                .set("groupDiameterMeters", BigDecimal.ZERO)
                .get();
    }

    private ReportCategoryMergeRule mergeRule(
            ReportCategory category,
            int linkRadiusMeters,
            int maxGroupDiameterMeters,
            String minEmbeddingSimilarity,
            String autoMergeThreshold) {
        return ReportCategoryMergeRule.create(
                category,
                linkRadiusMeters,
                maxGroupDiameterMeters,
                new BigDecimal(minEmbeddingSimilarity),
                new BigDecimal(autoMergeThreshold));
    }

    private Report report(
            String title,
            String summary,
            String roadAddress,
            String jibunAddress,
            User user,
            ReportCategory category,
            Department department,
            IssueGroup issueGroup,
            ReportStatus status,
            boolean representative) {
        return entity(Report.class)
                .set("title", title)
                .set("contents", "{\"summary\":\"" + summary + "\",\"source\":\"dummy-seed\",\"urgency\":\"normal\"}")
                .set("latitude", randomLatitude())
                .set("longitude", randomLongitude())
                .set("embedding", dummyEmbedding(title))
                .set("roadAddress", roadAddress)
                .set("jibunAddress", jibunAddress)
                .set("sido", "경기도")
                .set("sigungu", "용인시 수지구")
                .set("eupmyeondong", "풍덕천동")
                .set("occurredAt", LocalDateTime.now().minusHours(4))
                .set("riskScore", riskScoreFor(category.getCategoryCode()))
                .set("assignmentReason", "초기 더미 데이터용 담당 부서 배정 근거입니다.")
                .set("status", status)
                .set("visibility", ReportVisibility.PUBLIC)
                .set("isDeleted", false)
                .set("isRepresentative", representative)
                .set("user", user)
                .set("category", category)
                .set("issueGroup", issueGroup)
                .set("department", department)
                .get();
    }

    private Report report(
            String title,
            String summary,
            String latitude,
            String longitude,
            String roadAddress,
            String jibunAddress,
            String riskScore,
            User user,
            ReportCategory category,
            Department department,
            IssueGroup issueGroup,
            ReportStatus status,
            boolean representative) {
        return entity(Report.class)
                .set("title", title)
                .set("contents", "{\"summary\":\"" + summary + "\",\"source\":\"fixed-seed\",\"urgency\":\"normal\"}")
                .set("latitude", new BigDecimal(latitude))
                .set("longitude", new BigDecimal(longitude))
                .set("embedding", dummyEmbedding(title))
                .set("roadAddress", roadAddress)
                .set("jibunAddress", jibunAddress)
                .set("sido", "경기도")
                .set("sigungu", "용인시 수지구")
                .set("eupmyeondong", "죽전동")
                .set("occurredAt", LocalDateTime.now().minusHours(1))
                .set("riskScore", new BigDecimal(riskScore))
                .set("assignmentReason", "요청 좌표 기반 시딩 데이터용 부서 배정입니다.")
                .set("status", status)
                .set("visibility", ReportVisibility.PUBLIC)
                .set("isDeleted", false)
                .set("isRepresentative", representative)
                .set("user", user)
                .set("category", category)
                .set("issueGroup", issueGroup)
                .set("department", department)
                .get();
    }

    private String dummyEmbedding(String seedText) {
        int seed = Math.abs(seedText.hashCode());
        StringBuilder embedding = new StringBuilder("[");
        for (int index = 0; index < 1536; index++) {
            if (index > 0) {
                embedding.append(',');
            }
            double value = (((seed + index * 37) % 2000) - 1000) / 10000.0;
            embedding.append(String.format(Locale.ROOT, "%.6f", value));
        }
        return embedding.append(']').toString();
    }

    private ReportImage reportImage(Report report, String imageUrl, int sortOrder) {
        return entity(ReportImage.class)
                .set("imageUrl", imageUrl)
                .set("sortOrder", sortOrder)
                .set("report", report)
                .get();
    }

    private ReportReactionLog reportReactionLog(Report report, User user, ReportReactionType reactionType) {
        return entity(ReportReactionLog.class)
                .set("reactionType", reactionType)
                .set("report", report)
                .set("user", user)
                .get();
    }

    private ReportStatusHistory reportStatusHistory(
            Report report,
            User user,
            ReportStatus previousStatus,
            ReportStatus newStatus,
            String reason) {
        return entity(ReportStatusHistory.class)
                .set("previousStatus", previousStatus)
                .set("newStatus", newStatus)
                .set("reason", reason)
                .set("report", report)
                .set("user", user)
                .set("department", report.getDepartment())
                .get();
    }

    private ChatbotSession chatbotSession(User user, String title) {
        return entity(ChatbotSession.class)
                .set("title", title)
                .set("createdAt", LocalDateTime.now().minusMinutes(30))
                .set("user", user)
                .get();
    }

    private ChatbotMessage chatbotMessage(ChatbotSession session, ChatbotSenderType senderType, String message) {
        return entity(ChatbotMessage.class)
                .set("senderType", senderType)
                .set("message", message)
                .set("createdAt", LocalDateTime.now().minusMinutes(20))
                .set("session", session)
                .get();
    }

    private Notification notification(
            User user,
            Report report,
            IssueGroup issueGroup,
            NotificationType notificationType,
            boolean read) {
        return entity(Notification.class)
                .set("notificationType", notificationType)
                .set("payload", "{\"title\":\"신고 상태 알림\",\"message\":\"담당 부서가 신고를 확인하고 있습니다.\"}")
                .set("isRead", read)
                .set("user", user)
                .set("report", report)
                .set("issueGroup", issueGroup)
                .get();
    }

    private String statusReason(int index) {
        return switch (index) {
            case 1 -> "시민 신고가 정상 접수되었습니다.";
            case 2 -> "담당 부서에서 현장 확인을 시작했습니다.";
            default -> "유사 신고와 묶어 처리 중입니다.";
        };
    }

    // 수지구 범위 랜덤 좌표 (위도 37.31~37.34, 경도 127.09~127.12)
    private BigDecimal randomLatitude() {
        return randomInRange(37.31, 37.34, 7);
    }

    private BigDecimal randomLongitude() {
        return randomInRange(127.09, 127.12, 7);
    }

    // 카테고리별 위험도: 쓰레기 무단투기 < 50, 맨홀·불법주차 등 시설/질서 50~70, 인명피해 70 이상
    private BigDecimal riskScoreFor(String categoryCode) {
        return switch (categoryCode) {
            case "WASTE_AND_DEBRIS" -> randomInRange(30.0, 50.0, 2);
            case "VULNERABLE_PERSON_RISK", "FIRE_RISK", "GAS_ELECTRIC_RISK" -> randomInRange(70.0, 90.0, 2);
            default -> randomInRange(50.0, 70.0, 2);
        };
    }

    private BigDecimal randomInRange(double min, double max, int scale) {
        double value = min + random.nextDouble() * (max - min);
        return new BigDecimal(String.format(Locale.ROOT, "%." + scale + "f", value));
    }

    private <T> List<T> findAllSorted(JpaRepository<T, Long> repository) {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(this::idOf, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private Long idOf(Object value) {
        try {
            Field field = value.getClass().getDeclaredField("id");
            field.setAccessible(true);
            return (Long) field.get(value);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to read seed entity id.", exception);
        }
    }

    private <T> EntityBuilder<T> entity(Class<T> type) {
        return new EntityBuilder<>(instantiate(type));
    }

    private <T> EntityBuilder<T> entity(T value) {
        return new EntityBuilder<>(value);
    }

    private static <T> T instantiate(Class<T> type) {
        try {
            Constructor<T> constructor = type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create seed entity: " + type.getSimpleName(), exception);
        }
    }

    private record FixedIssueGroupSeed(
            String title,
            String content,
            String latitude,
            String longitude,
            String riskScore,
            int reportCount) {
    }

    private record FixedReportSeed(
            int issueGroupIndex,
            String title,
            String summary,
            String latitude,
            String longitude,
            String roadAddress,
            String jibunAddress,
            String riskScore,
            String categoryCode,
            String agencyName,
            String departmentName,
            ReportStatus status) {
    }

    private record SeedTemplate(List<Department> departments, List<ReportCategory> reportCategories) {

        private ReportCategory categoryByCode(String categoryCode) {
            return reportCategories.stream()
                    .filter(category -> categoryCode.equals(category.getCategoryCode()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Seed category not found: " + categoryCode));
        }

        private Department departmentByName(String agencyName, String departmentName) {
            return departments.stream()
                    .filter(department -> departmentName.equals(department.getName()))
                    .filter(department -> agencyName.equals(department.getAgencyType().getName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Seed department not found: " + agencyName + " / " + departmentName));
        }
    }

    private static class EntityBuilder<T> {

        private final T entity;

        private EntityBuilder(T entity) {
            this.entity = entity;
        }

        private EntityBuilder<T> set(String fieldName, Object value) {
            Class<?> current = entity.getClass();
            while (current != null) {
                try {
                    Field field = current.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(entity, value);
                    return this;
                } catch (NoSuchFieldException ignored) {
                    current = current.getSuperclass();
                } catch (IllegalAccessException exception) {
                    throw new IllegalStateException("Failed to set seed field: " + fieldName, exception);
                }
            }
            throw new IllegalArgumentException("Seed field not found: " + fieldName);
        }

        private T get() {
            return entity;
        }
    }
}
