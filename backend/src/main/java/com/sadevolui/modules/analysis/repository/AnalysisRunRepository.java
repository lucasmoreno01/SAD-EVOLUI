package com.sadevolui.modules.analysis.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sadevolui.modules.analysis.entity.AnalysisRun;

public interface AnalysisRunRepository extends JpaRepository<AnalysisRun, UUID> {
    List<AnalysisRun> findAllByRequestedByIdOrderByCreatedAtDesc(UUID userId);
    Optional<AnalysisRun> findByIdAndRequestedById(UUID id, UUID userId);
}
