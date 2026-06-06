package com.ssaika.ssiren.domain.report.entity;

import com.ssaika.ssiren.global.entity.BaseTime;
import com.ssaika.ssiren.global.enums.IssueGroupStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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
@Table(name = "issue_groups")
public class IssueGroup extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 150)
    private String title;

    @Column(columnDefinition = "text")
    private String content;

    @Column(name = "group_latitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal groupLatitude;

    @Column(name = "group_longitude", nullable = false, precision = 10, scale = 7)
    private BigDecimal groupLongitude;

    @Column(name = "report_count", nullable = false)
    private Integer reportCount;

    @Column(name = "yes_count", nullable = false)
    private Integer yesCount;

    @Column(name = "no_count", nullable = false)
    private Integer noCount;

    @Column(name = "unknown_count", nullable = false)
    private Integer unknownCount;

    @Column(name = "recent_reported_at", nullable = false)
    private LocalDateTime recentReportedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueGroupStatus status;

    @Column(name = "risk_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal riskScore;
}
