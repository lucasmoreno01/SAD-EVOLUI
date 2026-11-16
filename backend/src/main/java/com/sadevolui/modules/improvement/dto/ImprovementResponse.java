package com.sadevolui.modules.improvement.dto;

import java.time.Instant;
import java.util.UUID;
import com.sadevolui.modules.improvement.entity.ImprovementStatus;

public record ImprovementResponse(UUID id, UUID applicationId, String applicationName, String projectName, UUID issueGroupId, String issueGroupTitle, String title, String description, int impact, int urgency, int frequency, int effort, String assessmentOrigin, ImprovementStatus status, Instant createdAt, Instant updatedAt) { }
