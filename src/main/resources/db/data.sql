-- 기관 타입
INSERT INTO agency_type (id, name)
VALUES
    (1, '지자체'),
    (2, '경찰'),
    (3, '소방');


-- 부서
INSERT INTO departments (id, name, agency_type_id)
VALUES
    (1,  '교통행정과', 1),
    (2,  '도로관리과', 1),
    (3,  '청소행정과', 1),
    (4,  '환경과', 1),
    (5,  '도시안전과', 1),
    (6,  '시설관리과', 1),
    (7,  '관할 지구대', 2),
    (8,  '복지정책과', 1),
    (9,  '119안전센터', 3),
    (10, '민원실', 1);


-- 카테고리 대분류
INSERT INTO report_categories (
    id,
    category_code,
    category_name,
    department_id,
    parent_category_id
)
VALUES
    (1, 'TRAFFIC', '교통', 1, NULL),
    (2, 'ENVIRONMENT', '환경', 4, NULL),
    (3, 'FACILITY', '시설물', 6, NULL),
    (4, 'LIFE_INCONVENIENCE', '생활불편', 6, NULL),
    (5, 'PUBLIC_SAFETY', '치안', 7, NULL),
    (6, 'WELFARE', '복지', 8, NULL),
    (7, 'DISASTER_SAFETY', '재난안전', 9, NULL),
    (8, 'ETC', '기타', 10, NULL);


-- 카테고리 소분류
-- AI 반환 categoryCode
INSERT INTO report_categories (
    id,
    category_code,
    category_name,
    department_id,
    parent_category_id
)
VALUES
    (9,  'ILLEGAL_PARKING', '불법주정차', 1, 1),
    (10, 'ROAD_DAMAGE', '도로 파손', 2, 1),

    (11, 'TRASH_DUMPING', '쓰레기 무단투기', 3, 2),
    (12, 'ANIMAL_CARCASS', '동물 사체', 3, 2),
    (13, 'NOISE', '소음', 4, 2),

    (14, 'STREETLIGHT', '가로등 고장', 5, 3),
    (15, 'DANGEROUS_FACILITY', '위험 시설물', 6, 3),

    (16, 'FALL_RISK', '낙상 위험', 6, 4),

    (17, 'DRUNK_PERSON', '주취자', 7, 5),
    (18, 'YOUTH_RISK', '청소년 위험', 7, 5),
    (19, 'SUSPICIOUS', '수상한 상황', 7, 5),
    (20, 'HOMELESS', '노숙', 8, 6),
    (21, 'FIRE_EMERGENCY', '화재/응급', 9, 7),
    (22, 'ETC_OTHER', '기타', 10, 8),
    (23, 'INSUFFICIENT', '제보 불성립', 10, 8);


-- 카테고리별 중복 병합 기준
INSERT INTO report_category_merge_rules (
    category_id,
    link_radius_meters,
    max_group_diameter_meters,
    min_embedding_similarity,
    auto_merge_threshold
)
SELECT category.id, rule.link_radius_meters, rule.max_group_diameter_meters,
       rule.min_embedding_similarity, rule.auto_merge_threshold
FROM (
    VALUES
        ('ILLEGAL_PARKING', 30, 100, 0.80, 80),
        ('ROAD_DAMAGE', 50, 120, 0.78, 80),
        ('TRASH_DUMPING', 50, 120, 0.78, 80),
        ('ANIMAL_CARCASS', 30, 80, 0.80, 80),
        ('NOISE', 150, 400, 0.75, 82),
        ('STREETLIGHT', 20, 50, 0.82, 80),
        ('DANGEROUS_FACILITY', 50, 150, 0.78, 80),
        ('FALL_RISK', 50, 120, 0.78, 80),
        ('DRUNK_PERSON', 80, 200, 0.78, 82),
        ('YOUTH_RISK', 100, 250, 0.76, 82),
        ('SUSPICIOUS', 100, 250, 0.76, 82),
        ('HOMELESS', 100, 300, 0.75, 82),
        ('FIRE_EMERGENCY', 150, 500, 0.70, 85),
        ('ETC_OTHER', 30, 80, 0.85, 90)
) AS rule(category_code, link_radius_meters, max_group_diameter_meters, min_embedding_similarity, auto_merge_threshold)
JOIN report_categories category
    ON category.category_code = rule.category_code;
