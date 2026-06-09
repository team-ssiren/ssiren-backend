package com.ssaika.ssiren.domain.report.entity;

import com.ssaika.ssiren.global.entity.BaseTime;
import com.ssaika.ssiren.global.enums.IssueGroupStatus;
import com.ssaika.ssiren.global.enums.ReportReactionType;
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

    @Column(name = "group_diameter_meters", nullable = false, precision = 10, scale = 2)
    private BigDecimal groupDiameterMeters;

    public static IssueGroup create(
        String title,
        String content,
        BigDecimal groupLatitude,
        BigDecimal groupLongitude,
        LocalDateTime recentReportedAt,
        BigDecimal riskScore) {
        return IssueGroup.builder()
            .title(title)
            .content(content)
            .groupLatitude(groupLatitude)
            .groupLongitude(groupLongitude)
            .reportCount(1)
            .yesCount(0)
            .noCount(0)
            .unknownCount(0)
            .recentReportedAt(recentReportedAt)
            .status(IssueGroupStatus.ACTIVE)
            .riskScore(riskScore)
            .groupDiameterMeters(BigDecimal.ZERO)
            .build();
    }

    public void decreaseReportCount() {
        if (reportCount > 0) {
            reportCount--;
        }
    }

    public void mergeReport(
        BigDecimal groupLatitude,
        BigDecimal groupLongitude,
        BigDecimal groupDiameterMeters,
        BigDecimal riskScore,
        LocalDateTime recentReportedAt) {
        this.reportCount++;
        this.groupLatitude = groupLatitude;
        this.groupLongitude = groupLongitude;
        this.groupDiameterMeters = groupDiameterMeters;
        if (riskScore.compareTo(this.riskScore) > 0) {
            this.riskScore = riskScore;
        }
        this.recentReportedAt = recentReportedAt;
    }

    public void applyReaction(
        ReportReactionType previousReactionType,
        ReportReactionType newReactionType) {
        if (previousReactionType == newReactionType) {
            return;
        }

        decreaseReactionCount(previousReactionType);
        increaseReactionCount(newReactionType);
    }

    private void increaseReactionCount(ReportReactionType reactionType) {
        if (reactionType == ReportReactionType.YES) {
            yesCount++;
            return;
        }
        if (reactionType == ReportReactionType.NO) {
            noCount++;
            return;
        }
        if (reactionType == ReportReactionType.UNKNOWN) {
            unknownCount++;
        }
    }

    private void decreaseReactionCount(ReportReactionType reactionType) {
        if (reactionType == ReportReactionType.YES && yesCount > 0) {
            yesCount--;
            return;
        }
        if (reactionType == ReportReactionType.NO && noCount > 0) {
            noCount--;
            return;
        }
        if (reactionType == ReportReactionType.UNKNOWN && unknownCount > 0) {
            unknownCount--;
        }
    }
}
