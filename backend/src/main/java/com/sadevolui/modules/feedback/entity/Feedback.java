package com.sadevolui.modules.feedback.entity;

import java.time.Instant;
import java.util.UUID;
import com.sadevolui.modules.application.entity.ManagedApplication;
import com.sadevolui.modules.user.entity.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "feedbacks")
@Getter
@NoArgsConstructor
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private ManagedApplication application;

    @Column(nullable = false)
    private short rating;

    @Column(nullable = false, length = 4000)
    private String comment;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private UserAccount createdBy;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FeedbackSource source;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public Feedback(ManagedApplication application, UserAccount createdBy, FeedbackSource source, int rating, String comment, Instant submittedAt) {
        this.createdBy = createdBy;
        this.source = source;
        update(application, rating, comment, submittedAt);
    }

    public void update(ManagedApplication application, int rating, String comment, Instant submittedAt) {
        this.application = application;
        this.rating = (short) rating;
        this.comment = comment.trim();
        this.submittedAt = submittedAt;
    }
}
