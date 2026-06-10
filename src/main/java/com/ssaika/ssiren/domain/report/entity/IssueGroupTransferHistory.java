package com.ssaika.ssiren.domain.report.entity;

import com.ssaika.ssiren.domain.agency.entity.Department;
import com.ssaika.ssiren.domain.user.entity.User;
import com.ssaika.ssiren.global.entity.BaseTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder(access = AccessLevel.PRIVATE)
@Table(name = "issue_group_transfer_histories")
public class IssueGroupTransferHistory extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IssueGroupTransferHistoryStatus status;

    @Column(name = "request_reason", nullable = false, length = 500)
    private String requestReason;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "response_reason", length = 500)
    private String responseReason;

    @Column(name = "response_at")
    private LocalDateTime responseAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_group_id", nullable = false)
    private IssueGroup issueGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_department_id", nullable = false)
    private Department fromDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_department_id", nullable = false)
    private Department targetDepartment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_user_id", nullable = false)
    private User requestUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_user_id")
    private User responseUser;

    public static IssueGroupTransferHistory create(
            IssueGroup issueGroup,
            Department fromDepartment,
            Department targetDepartment,
            User requestUser,
            String requestReason
    ) {
        return IssueGroupTransferHistory.builder()
                .status(IssueGroupTransferHistoryStatus.REQUESTED)
                .requestReason(requestReason)
                .requestedAt(LocalDateTime.now())
                .issueGroup(issueGroup)
                .fromDepartment(fromDepartment)
                .targetDepartment(targetDepartment)
                .requestUser(requestUser)
                .build();
    }

    public void accept(User responseUser, String responseReason) {
        this.status = IssueGroupTransferHistoryStatus.ACCEPTED;
        this.responseUser = responseUser;
        this.responseReason = responseReason;
        this.responseAt = LocalDateTime.now();
    }

    public void reject(User responseUser, String responseReason) {
        this.status = IssueGroupTransferHistoryStatus.REJECTED;
        this.responseUser = responseUser;
        this.responseReason = responseReason;
        this.responseAt = LocalDateTime.now();
    }

    public void cancel(User responseUser, String responseReason) {
        this.status = IssueGroupTransferHistoryStatus.CANCELED;
        this.responseUser = responseUser;
        this.responseReason = responseReason;
        this.responseAt = LocalDateTime.now();
    }
}
