package com.ssaika.ssiren.domain.report.entity;

import com.ssaika.ssiren.domain.agency.entity.Department;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.global.entity.BaseTime;
import com.ssaika.ssiren.global.enums.ReportStatus;
import com.ssaika.ssiren.global.enums.ReportVisibility;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "reports")
public class Report extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String contents;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "road_address", nullable = false)
    private String roadAddress;

    @Column(name = "jibun_address", nullable = false)
    private String jibunAddress;

    @Column(nullable = false, length = 50)
    private String sido;

    @Column(nullable = false, length = 50)
    private String sigungu;

    @Column(nullable = false, length = 50)
    private String eupmyeondong;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(name = "risk_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportVisibility visibility;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "is_representative", nullable = false)
    private Boolean isRepresentative;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private ReportCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_group_id", nullable = false)
    private IssueGroup issueGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;
}
