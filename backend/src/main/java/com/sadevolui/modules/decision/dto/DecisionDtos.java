package com.sadevolui.modules.decision.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class DecisionDtos {
    private DecisionDtos() { }
    public record Weights(BigDecimal impact, BigDecimal urgency, BigDecimal frequency, BigDecimal effort) { }
    public record Score(UUID improvementId, String title, int impact, int urgency, int frequency, int effort, BigDecimal normalizedImpact, BigDecimal normalizedUrgency, BigDecimal normalizedFrequency, BigDecimal normalizedEffort, BigDecimal finalScore, int ranking) { }
    public record Run(UUID id, String name, Weights weights, Instant createdAt, List<Score> ranking, String normalization, String criteriaMeaning) { }
    public record RunSummary(UUID id, String name, Instant createdAt, int alternativeCount, BigDecimal topScore) { }
}
