package com.sadevolui.modules.analysis.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sadevolui.modules.analysis.entity.IssueGroupItem;

public interface IssueGroupItemRepository extends JpaRepository<IssueGroupItem, UUID> {
    List<IssueGroupItem> findAllByIssueGroupId(UUID groupId);
    @Query("select count(item) from IssueGroupItem item where item.issueGroup.id = :groupId and item.feedbackAnalysis.feedback.application.id = :applicationId")
    long countFeedbacksForApplication(@Param("groupId") UUID groupId, @Param("applicationId") UUID applicationId);
}
