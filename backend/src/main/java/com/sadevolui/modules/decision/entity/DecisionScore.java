package com.sadevolui.modules.decision.entity;

import java.math.BigDecimal;
import java.util.UUID;
import com.sadevolui.modules.improvement.entity.Improvement;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "decision_scores") @Getter @NoArgsConstructor
public class DecisionScore {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "decision_run_id", nullable = false) private DecisionRun run;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "improvement_id", nullable = false) private Improvement improvement;
    @Column(name = "improvement_title", nullable = false, length = 160) private String improvementTitle;
    @Column(name = "impact_value", nullable = false) private short impactValue;
    @Column(name = "urgency_value", nullable = false) private short urgencyValue;
    @Column(name = "frequency_value", nullable = false) private short frequencyValue;
    @Column(name = "effort_value", nullable = false) private short effortValue;
    @Column(name = "normalized_impact", precision = 10, scale = 8, nullable = false) private BigDecimal normalizedImpact;
    @Column(name = "normalized_urgency", precision = 10, scale = 8, nullable = false) private BigDecimal normalizedUrgency;
    @Column(name = "normalized_frequency", precision = 10, scale = 8, nullable = false) private BigDecimal normalizedFrequency;
    @Column(name = "normalized_effort", precision = 10, scale = 8, nullable = false) private BigDecimal normalizedEffort;
    @Column(name = "final_score", precision = 12, scale = 8, nullable = false) private BigDecimal finalScore;
    @Column(nullable = false) private int ranking;
    public DecisionScore(DecisionRun run, Improvement improvement, BigDecimal normalizedImpact, BigDecimal normalizedUrgency, BigDecimal normalizedFrequency, BigDecimal normalizedEffort, BigDecimal finalScore, int ranking) {
        this.run = run; this.improvement = improvement; this.improvementTitle = improvement.getTitle();
        this.impactValue = (short) improvement.getImpact(); this.urgencyValue = (short) improvement.getUrgency();
        this.frequencyValue = (short) improvement.getFrequency(); this.effortValue = (short) improvement.getEffort();
        this.normalizedImpact = normalizedImpact; this.normalizedUrgency = normalizedUrgency; this.normalizedFrequency = normalizedFrequency;
        this.normalizedEffort = normalizedEffort; this.finalScore = finalScore; this.ranking = ranking;
    }
}
