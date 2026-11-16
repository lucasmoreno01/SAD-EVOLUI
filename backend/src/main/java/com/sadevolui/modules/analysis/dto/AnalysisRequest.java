package com.sadevolui.modules.analysis.dto;

import java.util.UUID;

public record AnalysisRequest(UUID projectId, UUID applicationId) { }
