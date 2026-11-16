package com.sadevolui.modules.improvement.entity;

import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.sadevolui.modules.analysis.entity.IssueGroup;
import com.sadevolui.modules.application.entity.ManagedApplication;
import com.sadevolui.modules.user.entity.UserAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "improvements") @Getter @NoArgsConstructor
public class Improvement {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "application_id", nullable = false) private ManagedApplication application;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "issue_group_id") private IssueGroup issueGroup;
    @Column(nullable = false, length = 160) private String title;
    @Column(nullable = false, length = 2000) private String description;
    @Column(nullable = false) private short impact;
    @Column(nullable = false) private short urgency;
    @Column(nullable = false) private short effort;
    @Column(name = "assessment_origin", nullable = false, length = 500) private String assessmentOrigin;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private ImprovementStatus status;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false) private UserAccount createdBy;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public Improvement(ManagedApplication application, IssueGroup issueGroup, String title, String description, int impact, int urgency, int effort, String assessmentOrigin, ImprovementStatus status, UserAccount createdBy) {
        this.createdBy = createdBy;
        update(application, issueGroup, title, description, impact, urgency, effort, assessmentOrigin, status);
    }
    public void update(ManagedApplication application, IssueGroup issueGroup, String title, String description, int impact, int urgency, int effort, String assessmentOrigin, ImprovementStatus status) {
        this.application = application; this.issueGroup = issueGroup;
        this.title = title.trim(); this.description = description.trim(); this.impact = (short) impact; this.urgency = (short) urgency;
        this.effort = (short) effort; this.assessmentOrigin = assessmentOrigin.trim(); this.status = status;
    }
    public void updateStatus(ImprovementStatus status) { this.status = status; }
}
