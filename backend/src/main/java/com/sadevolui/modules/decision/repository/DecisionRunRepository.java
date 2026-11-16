package com.sadevolui.modules.decision.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sadevolui.modules.decision.entity.DecisionRun;

public interface DecisionRunRepository extends JpaRepository<DecisionRun, UUID> {
    List<DecisionRun> findAllByCreatedByIdOrderByCreatedAtDesc(UUID userId);
    Optional<DecisionRun> findByIdAndCreatedById(UUID id, UUID userId);
    Optional<DecisionRun> findFirstByCreatedByIdOrderByCreatedAtDesc(UUID userId);
}
