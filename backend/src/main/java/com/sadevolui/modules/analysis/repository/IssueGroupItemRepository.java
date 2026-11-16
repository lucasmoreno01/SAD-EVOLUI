package com.sadevolui.modules.analysis.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sadevolui.modules.analysis.entity.IssueGroupItem;

public interface IssueGroupItemRepository extends JpaRepository<IssueGroupItem, UUID> {
    List<IssueGroupItem> findAllByIssueGroupId(UUID groupId);
}
