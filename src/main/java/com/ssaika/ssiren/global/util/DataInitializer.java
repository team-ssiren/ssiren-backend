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
                        agencyType("지자체"),
                        agencyType("경찰"),
                        agencyType("소방")))
                : findAllSorted(agencyTypeRepository);

        List<Department> departments = departmentRepository.count() == 0
                ? departmentRepository.saveAll(List.of(
                        department("교통행정과", agencyTypes.get(0)),
                        department("도로관리과", agencyTypes.get(0)),
                        department("청소행정과", agencyTypes.get(0)),
                        department("환경과", agencyTypes.get(0)),
                        department("도시안전과", agencyTypes.get(0)),
                        department("시설관리과", agencyTypes.get(0)),
                        department("관할 지구대", agencyTypes.get(1)),
                        department("복지정책과", agencyTypes.get(0)),
                        department("119안전센터", agencyTypes.get(2)),
                        department("민원실", agencyTypes.get(0))))
                : findAllSorted(departmentRepository);

        List<ReportCategory> reportCategories = reportCategoryRepository.count() == 0
                ? saveReportCategoryTemplate(departments)
                : findAllSorted(reportCategoryRepository);
        reportCategories = saveInsufficientCategoryIfNeeded(reportCategories, departments);

        saveReportCategoryMergeRulesIfNeeded(reportCategories);

        return new SeedTemplate(departments, reportCategories);
    }

    private List<ReportCategory> saveReportCategoryTemplate(List<Department> departments) {
        List<ReportCategory> parents = reportCategoryRepository.saveAll(List.of(
                reportCategory("TRAFFIC", "교통", departments.get(0), null),
                reportCategory("ENVIRONMENT", "환경", departments.get(3), null),
                reportCategory("FACILITY", "시설물", departments.get(5), null),
                reportCategory("LIFE_INCONVENIENCE", "생활불편", departments.get(5), null),
                reportCategory("PUBLIC_SAFETY", "치안", departments.get(6), null),
                reportCategory("WELFARE", "복지", departments.get(7), null),
                reportCategory("DISASTER_SAFETY", "재난안전", departments.get(8), null),
                reportCategory("ETC", "기타", departments.get(9), null)));

        List<ReportCategory> children = reportCategoryRepository.saveAll(List.of(
                reportCategory("ILLEGAL_PARKING", "불법주정차", departments.get(0), parents.get(0)),
                reportCategory("ROAD_DAMAGE", "도로 파손", departments.get(1), parents.get(0)),
                reportCategory("TRASH_DUMPING", "쓰레기 무단투기", departments.get(2), parents.get(1)),
                reportCategory("ANIMAL_CARCASS", "동물 사체", departments.get(2), parents.get(1)),
                reportCategory("NOISE", "소음", departments.get(3), parents.get(1)),
                reportCategory("STREETLIGHT", "가로등 고장", departments.get(4), parents.get(2)),
                reportCategory("DANGEROUS_FACILITY", "위험 시설물", departments.get(5), parents.get(2)),
                reportCategory("FALL_RISK", "낙상 위험", departments.get(5), parents.get(3)),
                reportCategory("DRUNK_PERSON", "주취자", departments.get(6), parents.get(4)),
                reportCategory("YOUTH_RISK", "청소년 위험", departments.get(6), parents.get(4)),
                reportCategory("SUSPICIOUS", "수상한 상황", departments.get(6), parents.get(4)),
                reportCategory("HOMELESS", "노숙", departments.get(7), parents.get(5)),
                reportCategory("FIRE_EMERGENCY", "화재/응급", departments.get(8), parents.get(6)),
                reportCategory("ETC_OTHER", "기타", departments.get(9), parents.get(7)),
                reportCategory("INSUFFICIENT", "제보 불성립", departments.get(9), parents.get(7))));

        List<ReportCategory> categories = new ArrayList<>(parents);
        categories.addAll(children);
        return categories;
    }

    private List<ReportCategory> saveInsufficientCategoryIfNeeded(
            List<ReportCategory> reportCategories,
            List<Department> departments) {
        boolean exists = reportCategories.stream()
                .anyMatch(category -> "INSUFFICIENT".equals(category.getCategoryCode()));
        if (exists) {
            return reportCategories;
        }

        ReportCategory parent = reportCategories.stream()
                .filter(category -> "ETC".equals(category.getCategoryCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seed category not found: ETC"));
        Department department = departments.stream()
                .filter(value -> "민원실".equals(value.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Seed department not found: 민원실"));
        ReportCategory insufficient = reportCategoryRepository.save(
                reportCategory("INSUFFICIENT", "제보 불성립", department, parent));

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
                mergeRule(template.categoryByCode("ROAD_DAMAGE"), 50, 120, "0.78", "80"),
                mergeRule(template.categoryByCode("TRASH_DUMPING"), 50, 120, "0.78", "80"),
                mergeRule(template.categoryByCode("ANIMAL_CARCASS"), 30, 80, "0.80", "80"),
                mergeRule(template.categoryByCode("NOISE"), 150, 400, "0.75", "82"),
                mergeRule(template.categoryByCode("STREETLIGHT"), 20, 50, "0.82", "80"),
                mergeRule(template.categoryByCode("DANGEROUS_FACILITY"), 50, 150, "0.78", "80"),
                mergeRule(template.categoryByCode("FALL_RISK"), 50, 120, "0.78", "80"),
                mergeRule(template.categoryByCode("DRUNK_PERSON"), 80, 200, "0.78", "82"),
                mergeRule(template.categoryByCode("YOUTH_RISK"), 100, 250, "0.76", "82"),
                mergeRule(template.categoryByCode("SUSPICIOUS"), 100, 250, "0.76", "82"),
                mergeRule(template.categoryByCode("HOMELESS"), 100, 300, "0.75", "82"),
                mergeRule(template.categoryByCode("FIRE_EMERGENCY"), 150, 500, "0.70", "85"),
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
                issueGroup("역삼역 인근 보도블록 파손", "출근 시간대 보행자가 반복적으로 불편을 겪는 구간입니다.", "37.5008450", "127.0366500", "72.40"),
                issueGroup("삼성동 골목 쓰레기 무단투기", "상가 뒤편에 생활 폐기물과 음식물 쓰레기가 반복 적치됩니다.", "37.5129470", "127.0564090", "58.20"),
                issueGroup("논현동 어린이보호구역 불법주정차", "등교 시간대 횡단보도 시야가 가려지는 상황입니다.", "37.5112640", "127.0286250", "81.70")));

        List<Report> reports = reportRepository.saveAll(createReports(citizens, issueGroups, template));

        saveReportChildren(reports, users, operators.get(0));
        saveChatbotData(users);
        saveNotifications(users, reports);
    }

    private List<Report> createReports(List<User> citizens, List<IssueGroup> issueGroups, SeedTemplate template) {
        return List.of(
                report("보도블록이 내려앉아 발목을 접질릴 뻔했습니다.", "보도블록 파손", "37.5008010", "127.0366220", "서울 강남구 테헤란로 152", "서울 강남구 역삼동 737", citizens.get(0), template.categoryByCode("ROAD_DAMAGE"), issueGroups.get(0), ReportStatus.SUBMITTED, true),
                report("비 오는 날마다 같은 보도블록 구간에 물이 고입니다.", "침하된 보도블록", "37.5009120", "127.0367010", "서울 강남구 테헤란로 156", "서울 강남구 역삼동 738", citizens.get(1), template.categoryByCode("ROAD_DAMAGE"), issueGroups.get(0), ReportStatus.CHECKING, false),
                report("파손 구간 옆 안내 표시가 없어 야간에 위험합니다.", "보행 안전 표시 필요", "37.5007550", "127.0365900", "서울 강남구 테헤란로 150", "서울 강남구 역삼동 736", citizens.get(2), template.categoryByCode("FALL_RISK"), issueGroups.get(0), ReportStatus.IN_PROGRESS, false),
                report("퇴근길마다 같은 위치에 생활 쓰레기가 쌓입니다.", "쓰레기 무단투기", "37.5129130", "127.0563770", "서울 강남구 영동대로 513", "서울 강남구 삼성동 159", citizens.get(0), template.categoryByCode("TRASH_DUMPING"), issueGroups.get(1), ReportStatus.RECEIVED, true),
                report("상가 뒤편에 종량제 봉투가 아닌 폐기물이 방치되어 있습니다.", "폐기물 방치", "37.5128700", "127.0564450", "서울 강남구 영동대로 517", "서울 강남구 삼성동 160", citizens.get(1), template.categoryByCode("TRASH_DUMPING"), issueGroups.get(1), ReportStatus.SUBMITTED, false),
                report("음식물 쓰레기 냄새가 골목 전체로 퍼지고 있습니다.", "음식물 쓰레기 적치", "37.5129910", "127.0564910", "서울 강남구 영동대로 519", "서울 강남구 삼성동 161", citizens.get(2), template.categoryByCode("NOISE"), issueGroups.get(1), ReportStatus.IN_PROGRESS, false),
                report("어린이보호구역 횡단보도 앞 불법주정차입니다.", "불법주정차", "37.5112210", "127.0285960", "서울 강남구 학동로 120", "서울 강남구 논현동 142", citizens.get(0), template.categoryByCode("ILLEGAL_PARKING"), issueGroups.get(2), ReportStatus.CHECKING, true),
                report("등교 시간에 횡단보도 모퉁이를 차량이 막고 있습니다.", "보호구역 시야 방해", "37.5113180", "127.0286640", "서울 강남구 학동로 124", "서울 강남구 논현동 143", citizens.get(1), template.categoryByCode("ILLEGAL_PARKING"), issueGroups.get(2), ReportStatus.RECEIVED, false),
                report("어린이집 앞 정차 차량으로 보행 동선이 막힙니다.", "어린이집 앞 정차", "37.5111760", "127.0285480", "서울 강남구 학동로 118", "서울 강남구 논현동 141", citizens.get(2), template.categoryByCode("ILLEGAL_PARKING"), issueGroups.get(2), ReportStatus.SUBMITTED, false));
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
            officerDepartments.add(officerDepartment(operator, departments.get(0)));
            officerDepartments.add(officerDepartment(operator, departments.get(4)));
            officerDepartments.add(officerDepartment(operator, departments.get(9)));
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

    private ReportCategory reportCategory(String code, String name, Department department, ReportCategory parent) {
        return entity(ReportCategory.class)
                .set("categoryCode", code)
                .set("categoryName", name)
                .set("department", department)
                .set("parentCategory", parent)
                .get();
    }

    private User user(String email, String nickname, UserRole role, boolean alarmEnabled) {
        User user = User.createKakaoUser(email, nickname);
        return entity(user)
                .set("role", role)
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

    private IssueGroup issueGroup(String title, String content, String latitude, String longitude, String riskScore) {
        return entity(IssueGroup.class)
                .set("title", title)
                .set("content", content)
                .set("groupLatitude", new BigDecimal(latitude))
                .set("groupLongitude", new BigDecimal(longitude))
                .set("reportCount", 3)
                .set("yesCount", 2)
                .set("noCount", 0)
                .set("unknownCount", 1)
                .set("recentReportedAt", LocalDateTime.now().minusHours(2))
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
            String latitude,
            String longitude,
            String roadAddress,
            String jibunAddress,
            User user,
            ReportCategory category,
            IssueGroup issueGroup,
            ReportStatus status,
            boolean representative) {
        return entity(Report.class)
                .set("title", title)
                .set("contents", "{\"summary\":\"" + summary + "\",\"source\":\"dummy-seed\",\"urgency\":\"normal\"}")
                .set("latitude", new BigDecimal(latitude))
                .set("longitude", new BigDecimal(longitude))
                .set("embedding", dummyEmbedding(title))
                .set("roadAddress", roadAddress)
                .set("jibunAddress", jibunAddress)
                .set("sido", "서울특별시")
                .set("sigungu", "강남구")
                .set("eupmyeondong", "역삼동")
                .set("occurredAt", LocalDateTime.now().minusHours(4))
                .set("riskScore", new BigDecimal("64.50"))
                .set("status", status)
                .set("visibility", ReportVisibility.PUBLIC)
                .set("isDeleted", false)
                .set("isRepresentative", representative)
                .set("user", user)
                .set("category", category)
                .set("issueGroup", issueGroup)
                .set("department", category.getDepartment())
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

    private record SeedTemplate(List<Department> departments, List<ReportCategory> reportCategories) {

        private ReportCategory categoryByCode(String categoryCode) {
            return reportCategories.stream()
                    .filter(category -> categoryCode.equals(category.getCategoryCode()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Seed category not found: " + categoryCode));
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
