package com.sadevolui.modules.analysis.entity;

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

@Entity @Table(name = "issue_group_items") @NoArgsConstructor @Getter
public class IssueGroupItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private java.util.UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "issue_group_id", nullable = false) private IssueGroup issueGroup;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "feedback_analysis_id", nullable = false, unique = true) private FeedbackAnalysis feedbackAnalysis;
    public IssueGroupItem(IssueGroup issueGroup, FeedbackAnalysis feedbackAnalysis) { this.issueGroup = issueGroup; this.feedbackAnalysis = feedbackAnalysis; }
}
