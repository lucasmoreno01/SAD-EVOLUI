package com.sadevolui.modules.analysis.entity;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.Instant;
import com.sadevolui.modules.category.entity.Category;
import com.sadevolui.modules.feedback.entity.Feedback;
import com.sadevolui.modules.user.entity.UserAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "feedback_analyses") @Getter @NoArgsConstructor
public class FeedbackAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "analysis_run_id", nullable = false) private AnalysisRun run;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "feedback_id", nullable = false) private Feedback feedback;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private Sentiment sentiment;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id", nullable = false) private Category category;
    @Enumerated(EnumType.STRING) @Column(name = "classification_source", nullable = false, length = 20) private ClassificationSource classificationSource;
    @Column(precision = 5, scale = 4) private BigDecimal confidence;
    @Enumerated(EnumType.STRING) @Column(name = "original_sentiment", length = 20) private Sentiment originalSentiment;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "original_category_id") private Category originalCategory;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewed_by") private UserAccount reviewedBy;
    @Column(name = "reviewed_at") private Instant reviewedAt;

    public FeedbackAnalysis(AnalysisRun run, Feedback feedback, Sentiment sentiment, Category category) {
        this.run = run; this.feedback = feedback; this.sentiment = sentiment; this.category = category;
        this.classificationSource = ClassificationSource.AUTOMATIC;
    }
    public void correct(Sentiment sentiment, Category category, UserAccount reviewer) {
        if (classificationSource == ClassificationSource.AUTOMATIC) {
            this.originalSentiment = this.sentiment;
            this.originalCategory = this.category;
        }
        this.sentiment = sentiment; this.category = category; this.classificationSource = ClassificationSource.MANUAL; this.confidence = null;
        this.reviewedBy = reviewer; this.reviewedAt = Instant.now();
    }
}
