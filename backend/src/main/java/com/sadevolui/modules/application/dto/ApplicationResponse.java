package com.sadevolui.modules.application.dto;

import java.time.Instant;
import java.util.UUID;

public record ApplicationResponse(UUID id, UUID projectId, String projectName, String name, String description, Instant createdAt, Instant updatedAt) {
}
