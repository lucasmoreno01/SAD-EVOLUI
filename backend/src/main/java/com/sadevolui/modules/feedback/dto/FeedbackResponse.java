package com.sadevolui.modules.feedback.dto;

import java.time.Instant;
import java.util.UUID;

public record FeedbackResponse(UUID id, UUID applicationId, String applicationName, UUID projectId, String projectName, int rating, String comment, Instant submittedAt, Instant createdAt) {
}
