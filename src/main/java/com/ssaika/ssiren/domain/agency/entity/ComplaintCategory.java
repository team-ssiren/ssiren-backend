package com.ssaika.ssiren.domain.agency.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "complaint_categories")
public class ComplaintCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // AI 반환 코드
    @Column(name = "category_code", nullable = false, unique = true, length = 150)
    private String categoryCode;

    // 민원분류명
    @Column(nullable = false, length = 150)
    private String name;

    // 대분류
    @Enumerated(EnumType.STRING)
    @Column(name = "category_group", nullable = false, length = 50)
    private ComplaintCategoryGroup categoryGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}