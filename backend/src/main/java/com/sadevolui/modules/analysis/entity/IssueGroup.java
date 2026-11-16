package com.sadevolui.modules.analysis.entity;

import java.util.UUID;
import com.sadevolui.modules.category.entity.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "issue_groups") @Getter @NoArgsConstructor
public class IssueGroup {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "analysis_run_id", nullable = false) private AnalysisRun run;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "category_id", nullable = false) private Category category;
    @Column(nullable = false, length = 180) private String title;
    @Column(name = "feedback_count", nullable = false) private int feedbackCount;

    public IssueGroup(AnalysisRun run, Category category, String title, int feedbackCount) {
        this.run = run; this.category = category; this.title = title; this.feedbackCount = feedbackCount;
    }
}
