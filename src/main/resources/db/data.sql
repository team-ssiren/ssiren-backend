-- 기관 타입
INSERT INTO agency_type (id, name)
VALUES
    (1, '지자체'),
    (2, '경찰'),
    (3, '소방');


-- 부서
INSERT INTO departments (id, agency_name, name, agency_type_id)
VALUES
    (1, '지자체', '교통행정과', 1),
    (2, '지자체', '도로관리과', 1),
    (3, '지자체', '청소행정과', 1),
    (4, '지자체', '환경과', 1),
    (5, '지자체', '도시안전과', 1),
    (6, '지자체', '시설관리과', 1),
    (7, '경찰', '관할 지구대', 2),
    (8, '지자체', '복지정책과', 1),
    (9, '소방', '119안전센터', 3),
    (10, '지자체', '민원실', 1);


-- 민원 카테고리
INSERT INTO complaint_categories (
    id,
    category_code,
    name,
    category_group,
    department_id
)
VALUES
    (1, 'ILLEGAL_PARKING', '불법주정차', 'TRAFFIC', 1),
    (2, 'ROAD_DAMAGE', '도로 파손', 'TRAFFIC', 2),
    (3, 'TRASH_DUMPING', '쓰레기 무단투기', 'ENVIRONMENT', 3),
    (4, 'ANIMAL_CARCASS', '동물 사체', 'ENVIRONMENT', 3),
    (5, 'NOISE', '소음', 'ENVIRONMENT', 4),
    (6, 'STREETLIGHT', '가로등 고장', 'FACILITY', 5),
    (7, 'DANGEROUS_FACILITY', '위험 시설물', 'FACILITY', 6),
    (8, 'FALL_RISK', '낙상 위험', 'LIFE_INCONVENIENCE', 6),
    (9, 'DRUNK_PERSON', '주취자', 'PUBLIC_SAFETY', 7),
    (10, 'YOUTH_RISK', '청소년 위험', 'PUBLIC_SAFETY', 7),
    (11, 'SUSPICIOUS', '수상한 상황', 'PUBLIC_SAFETY', 7),
    (12, 'HOMELESS', '노숙', 'WELFARE', 8),
    (13, 'FIRE_EMERGENCY', '화재/응급', 'DISASTER_SAFETY', 9),
    (14, 'ETC_OTHER', '기타', 'ETC', 10);
