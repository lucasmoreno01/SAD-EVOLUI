package com.sadevolui.modules.analysis.dto;

import java.util.UUID;
import com.sadevolui.modules.analysis.entity.Sentiment;
import jakarta.validation.constraints.NotNull;

public record ClassificationCorrectionRequest(@NotNull Sentiment sentiment, @NotNull UUID categoryId) { }
