package com.sadevolui.modules.analysis.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AnalysisDtos {
    private AnalysisDtos() { }
    public record RunSummary(UUID id, UUID projectId, UUID applicationId, String methodVersion, Instant createdAt) { }
    public record Classification(UUID analysisId, UUID feedbackId, String comment, int rating, String sentiment, UUID categoryId, String category, String source, String originalSentiment, UUID originalCategoryId, String originalCategory, java.time.Instant reviewedAt) { }
    public record IssueGroup(UUID id, String title, UUID categoryId, String category, int feedbackCount, List<UUID> feedbackIds) { }
    public record RunDetail(UUID id, UUID projectId, UUID applicationId, String methodVersion, Instant createdAt, int feedbackCount, Map<String, Long> sentimentCounts, Map<String, Long> categoryCounts, List<Classification> classifications, List<IssueGroup> groups, String methodNotice) { }
}
