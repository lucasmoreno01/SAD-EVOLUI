package com.sadevolui.modules.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardSummaryResponse(long projects, long applications, long feedbacks, Double averageRating,
        Double medianRating, List<Integer> modeRatings, Integer minimumRating, Integer maximumRating,
        Double standardDeviation, Map<String, Long> sentimentCounts, Map<String, Double> sentimentPercentages,
        Map<String, Long> categoryCounts, List<MonthlyCount> feedbacksByMonth, List<IssueHighlight> recurringIssues,
        Map<String, Long> improvementStatuses, List<PriorityHighlight> latestPriorities) {
    public record MonthlyCount(String month, long count) { }
    public record IssueHighlight(String title, String category, long feedbackCount) { }
    public record PriorityHighlight(String title, BigDecimal score, int ranking) { }
}
