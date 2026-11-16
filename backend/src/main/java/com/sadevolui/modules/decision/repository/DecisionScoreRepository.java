package com.sadevolui.modules.decision.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sadevolui.modules.decision.entity.DecisionScore;

public interface DecisionScoreRepository extends JpaRepository<DecisionScore, UUID> {
    List<DecisionScore> findAllByRunIdOrderByRankingAsc(UUID runId);
}
