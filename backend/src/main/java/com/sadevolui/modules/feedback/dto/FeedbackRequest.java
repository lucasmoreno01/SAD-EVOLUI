package com.sadevolui.modules.feedback.dto;

import java.time.Instant;
import java.util.UUID;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

public record FeedbackRequest(
        @NotNull UUID applicationId,
        @Min(1) @Max(5) int rating,
        @NotBlank @Size(max = 4000) String comment,
        @NotNull @PastOrPresent Instant submittedAt) {
}
