package com.sadevolui.modules.application.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(
        @NotNull UUID projectId,
        @NotBlank @Size(max = 120) String name,
        @Size(max = 2000) String description) {
}
