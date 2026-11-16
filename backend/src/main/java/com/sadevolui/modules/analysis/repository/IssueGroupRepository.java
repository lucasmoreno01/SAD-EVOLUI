package com.sadevolui.modules.analysis.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sadevolui.modules.analysis.entity.IssueGroup;

public interface IssueGroupRepository extends JpaRepository<IssueGroup, UUID> {
    List<IssueGroup> findAllByRunIdOrderByFeedbackCountDesc(UUID runId);
    java.util.Optional<IssueGroup> findByIdAndRunRequestedById(UUID id, UUID userId);
}
