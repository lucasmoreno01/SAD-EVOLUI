package com.sadevolui.modules.decision.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import com.sadevolui.modules.user.entity.UserAccount;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name = "decision_runs") @Getter @NoArgsConstructor
public class DecisionRun {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false, length = 120) private String name;
    @Column(name = "impact_weight", precision = 6, scale = 5, nullable = false) private BigDecimal impactWeight;
    @Column(name = "urgency_weight", precision = 6, scale = 5, nullable = false) private BigDecimal urgencyWeight;
    @Column(name = "frequency_weight", precision = 6, scale = 5, nullable = false) private BigDecimal frequencyWeight;
    @Column(name = "effort_weight", precision = 6, scale = 5, nullable = false) private BigDecimal effortWeight;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false) private UserAccount createdBy;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    public DecisionRun(String name, BigDecimal impact, BigDecimal urgency, BigDecimal frequency, BigDecimal effort, UserAccount createdBy) {
        this.name = name.trim(); this.impactWeight = impact; this.urgencyWeight = urgency; this.frequencyWeight = frequency; this.effortWeight = effort; this.createdBy = createdBy;
    }
}
