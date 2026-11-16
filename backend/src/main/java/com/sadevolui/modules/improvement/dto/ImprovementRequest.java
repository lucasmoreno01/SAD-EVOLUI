package com.sadevolui.modules.improvement.dto;

import java.util.UUID;
import com.sadevolui.modules.improvement.entity.ImprovementStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ImprovementRequest(
        @NotNull UUID applicationId, UUID issueGroupId,
        @NotBlank @Size(max = 160) String title, @NotBlank @Size(max = 2000) String description,
        @Min(1) @Max(5) int impact, @Min(1) @Max(5) int urgency, @Min(1) @Max(5) int effort,
        @NotBlank @Size(max = 500) String assessmentOrigin, @NotNull ImprovementStatus status) { }
