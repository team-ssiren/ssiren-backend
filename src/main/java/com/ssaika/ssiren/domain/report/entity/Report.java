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
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Point;

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

    @JdbcTypeCode(SqlTypes.GEOGRAPHY)
    @Column(
        columnDefinition = "geography(Point,4326) generated always as "
            + "(ST_SetSRID(ST_MakePoint(longitude::double precision, latitude::double precision), 4326)::geography) stored",
        insertable = false,
        updatable = false)
    private Point location;

    @ColumnTransformer(write = "?::vector")
    @Column(columnDefinition = "vector(1024)")
    private String embedding;

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

    public static Report create(
        String title,
        String contents,
        BigDecimal latitude,
        BigDecimal longitude,
        String roadAddress,
        String jibunAddress,
        String sido,
        String sigungu,
        String eupmyeondong,
        LocalDateTime occurredAt,
        BigDecimal riskScore,
        ReportVisibility visibility,
        String embedding,
        Boolean isRepresentative,
        User user,
        ReportCategory category,
        IssueGroup issueGroup,
        Department department) {
        return Report.builder()
            .title(title)
            .contents(contents)
            .latitude(latitude)
            .longitude(longitude)
            .roadAddress(roadAddress)
            .jibunAddress(jibunAddress)
            .sido(sido)
            .sigungu(sigungu)
            .eupmyeondong(eupmyeondong)
            .occurredAt(occurredAt)
            .riskScore(riskScore)
            .status(ReportStatus.SUBMITTED)
            .visibility(visibility)
            .embedding(embedding)
            .isDeleted(false)
            .isRepresentative(isRepresentative)
            .user(user)
            .category(category)
            .issueGroup(issueGroup)
            .department(department)
            .build();
    }

    public void update(
        String title,
        String contents,
        ReportVisibility visibility) {
        if (title != null) {
            this.title = title;
        }
        if (contents != null) {
            this.contents = contents;
        }
        if (visibility != null) {
            this.visibility = visibility;
        }
    }

    public void markRepresentative() {
        this.isRepresentative = true;
    }

    public void unmarkRepresentative() {
        this.isRepresentative = false;
    }
}
