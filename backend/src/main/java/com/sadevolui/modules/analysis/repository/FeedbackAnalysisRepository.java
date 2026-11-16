package com.sadevolui.modules.analysis.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sadevolui.modules.analysis.entity.FeedbackAnalysis;

public interface FeedbackAnalysisRepository extends JpaRepository<FeedbackAnalysis, UUID> {
    List<FeedbackAnalysis> findAllByRunId(UUID runId);
    List<FeedbackAnalysis> findAllByRunRequestedById(UUID userId);
    Optional<FeedbackAnalysis> findByIdAndRunRequestedById(UUID id, UUID userId);
}
