package com.sadevolui.modules.decision.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecisionRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull @DecimalMin("0") @DecimalMax("10") BigDecimal impactWeight,
        @NotNull @DecimalMin("0") @DecimalMax("10") BigDecimal urgencyWeight,
        @NotNull @DecimalMin("0") @DecimalMax("10") BigDecimal frequencyWeight,
        @NotNull @DecimalMin("0") @DecimalMax("10") BigDecimal effortWeight,
        @NotEmpty List<UUID> improvementIds) { }
