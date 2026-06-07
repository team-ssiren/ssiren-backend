package com.ssaika.ssiren.domain.report.entity;

import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.global.entity.BaseTime;
import com.ssaika.ssiren.global.enums.ReportReactionType;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "report_reaction_logs")
public class ReportReactionLog extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type")
    private ReportReactionType reactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public static ReportReactionLog create(
        Report report,
        User user,
        ReportReactionType reactionType) {
        return ReportReactionLog.builder()
            .report(report)
            .user(user)
            .reactionType(reactionType)
            .build();
    }

    public ReportReactionType updateReactionType(ReportReactionType reactionType) {
        ReportReactionType previousReactionType = this.reactionType;
        this.reactionType = reactionType;

        return previousReactionType;
    }
}
