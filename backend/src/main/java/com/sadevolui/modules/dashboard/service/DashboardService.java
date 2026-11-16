package com.sadevolui.modules.dashboard.service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sadevolui.modules.analysis.entity.FeedbackAnalysis;
import com.sadevolui.modules.analysis.repository.*;
import com.sadevolui.modules.application.entity.ManagedApplication;
import com.sadevolui.modules.application.repository.ApplicationRepository;
import com.sadevolui.modules.category.entity.Category;
import com.sadevolui.modules.dashboard.dto.DashboardSummaryResponse;
import com.sadevolui.modules.dashboard.dto.DashboardSummaryResponse.*;
import com.sadevolui.modules.decision.entity.DecisionRun;
import com.sadevolui.modules.decision.repository.*;
import com.sadevolui.modules.feedback.entity.Feedback;
import com.sadevolui.modules.feedback.repository.FeedbackRepository;
import com.sadevolui.modules.improvement.entity.ImprovementStatus;
import com.sadevolui.modules.improvement.repository.ImprovementRepository;
import com.sadevolui.modules.project.repository.ProjectRepository;
import com.sadevolui.shared.exception.BadRequestException;
import com.sadevolui.shared.exception.ResourceNotFoundException;

@Service
public class DashboardService {
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final FeedbackRepository feedbackRepository;
    private final FeedbackAnalysisRepository feedbackAnalysisRepository;
    private final AnalysisRunRepository analysisRunRepository;
    private final IssueGroupRepository issueGroupRepository;
    private final IssueGroupItemRepository issueGroupItemRepository;
    private final ImprovementRepository improvementRepository;
    private final DecisionRunRepository decisionRunRepository;
    private final DecisionScoreRepository decisionScoreRepository;

    public DashboardService(ProjectRepository projectRepository, ApplicationRepository applicationRepository, FeedbackRepository feedbackRepository,
            FeedbackAnalysisRepository feedbackAnalysisRepository, AnalysisRunRepository analysisRunRepository, IssueGroupRepository issueGroupRepository,
            IssueGroupItemRepository issueGroupItemRepository, ImprovementRepository improvementRepository, DecisionRunRepository decisionRunRepository,
            DecisionScoreRepository decisionScoreRepository) {
        this.projectRepository = projectRepository; this.applicationRepository = applicationRepository; this.feedbackRepository = feedbackRepository;
        this.feedbackAnalysisRepository = feedbackAnalysisRepository; this.analysisRunRepository = analysisRunRepository;
        this.issueGroupRepository = issueGroupRepository; this.issueGroupItemRepository = issueGroupItemRepository;
        this.improvementRepository = improvementRepository; this.decisionRunRepository = decisionRunRepository; this.decisionScoreRepository = decisionScoreRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary(UUID ownerId, UUID projectId, UUID applicationId, LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) throw new BadRequestException("A data inicial deve ser anterior ou igual à data final.");
        if (projectId != null) projectRepository.findByIdAndOwnerId(projectId, ownerId).orElseThrow(() -> new ResourceNotFoundException("Projeto", projectId));
        ManagedApplication application = applicationId == null ? null : applicationRepository.findByIdAndProject_Owner_Id(applicationId, ownerId).orElseThrow(() -> new ResourceNotFoundException("Aplicativo", applicationId));
        if (application != null && projectId != null && !application.getProject().getId().equals(projectId)) throw new BadRequestException("O aplicativo não pertence ao projeto informado.");
        Instant fromAt = from == null ? Instant.parse("0001-01-01T00:00:00Z") : from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toAt = to == null ? Instant.parse("9999-12-31T23:59:59Z") : to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        List<Feedback> feedbacks = feedbackRepository.findForDashboard(ownerId, projectId, applicationId, fromAt, toAt);
        long projectCount = projectId != null || application != null ? 1 : projectRepository.countByOwnerId(ownerId);
        long applicationCount = application != null ? 1 : projectId != null ? applicationRepository.countByProject_IdAndProject_Owner_Id(projectId, ownerId) : applicationRepository.countByProject_Owner_Id(ownerId);

        List<Integer> ratings = feedbacks.stream().map(feedback -> (int) feedback.getRating()).sorted().toList();
        Double average = ratings.isEmpty() ? null : ratings.stream().mapToInt(Integer::intValue).average().orElseThrow();
        Double median = median(ratings);
        List<Integer> modes = modes(ratings);
        Integer min = ratings.isEmpty() ? null : ratings.get(0);
        Integer max = ratings.isEmpty() ? null : ratings.get(ratings.size() - 1);
        Double deviation = standardDeviation(ratings, average);

        Set<UUID> feedbackIds = new HashSet<>(); feedbacks.forEach(feedback -> feedbackIds.add(feedback.getId()));
        Map<UUID, FeedbackAnalysis> latest = new HashMap<>();
        feedbackAnalysisRepository.findAllByRunRequestedById(ownerId).stream().filter(item -> feedbackIds.contains(item.getFeedback().getId()))
                .sorted(Comparator.comparing(item -> item.getRun().getCreatedAt()))
                .forEach(item -> latest.put(item.getFeedback().getId(), item));
        Map<String, Long> sentimentCounts = new LinkedHashMap<>(Map.of("POSITIVE", 0L, "NEUTRAL", 0L, "NEGATIVE", 0L));
        Map<String, Long> categoryCounts = new TreeMap<>();
        latest.values().forEach(item -> { sentimentCounts.compute(item.getSentiment().name(), (key, value) -> value + 1); categoryCounts.merge(item.getCategory().getName(), 1L, Long::sum); });
        Map<String, Double> sentimentPercentages = new LinkedHashMap<>();
        long classified = latest.size();
        for (String key : sentimentCounts.keySet()) sentimentPercentages.put(key, classified == 0 ? 0d : sentimentCounts.get(key) * 100d / classified);

        Map<YearMonth, Long> monthMap = new TreeMap<>();
        YearMonth lastMonth = YearMonth.now(ZoneOffset.UTC);
        YearMonth firstMonth = from == null ? lastMonth.minusMonths(5) : YearMonth.from(from);
        YearMonth finalMonth = to == null ? lastMonth : YearMonth.from(to);
        for (YearMonth month = firstMonth; !month.isAfter(finalMonth); month = month.plusMonths(1)) monthMap.put(month, 0L);
        feedbacks.forEach(feedback -> { YearMonth month = YearMonth.from(feedback.getSubmittedAt().atZone(ZoneOffset.UTC)); if (monthMap.containsKey(month)) monthMap.compute(month, (key, value) -> value + 1); });
        List<MonthlyCount> monthly = monthMap.entrySet().stream().map(entry -> new MonthlyCount(entry.getKey().format(DateTimeFormatter.ofPattern("yyyy-MM")), entry.getValue())).toList();

        List<IssueHighlight> issues = latestIssueGroups(ownerId, projectId, applicationId, feedbackIds);
        Map<String, Long> improvementStatuses = new LinkedHashMap<>();
        var scopedImprovements = improvementRepository.findAllOwned(ownerId).stream()
                .filter(item -> projectId == null || item.getApplication().getProject().getId().equals(projectId))
                .filter(item -> applicationId == null || item.getApplication().getId().equals(applicationId)).toList();
        for (ImprovementStatus status : ImprovementStatus.values()) {
            long count = scopedImprovements.stream().filter(item -> item.getStatus() == status).count();
            improvementStatuses.put(status.name(), count);
        }
        List<PriorityHighlight> priorities = decisionRunRepository.findFirstByCreatedByIdOrderByCreatedAtDesc(ownerId)
                .map(run -> decisionScoreRepository.findAllByRunIdOrderByRankingAsc(run.getId()).stream()
                        .filter(score -> projectId == null || score.getImprovement().getApplication().getProject().getId().equals(projectId))
                        .filter(score -> applicationId == null || score.getImprovement().getApplication().getId().equals(applicationId)).limit(5)
                        .map(score -> new PriorityHighlight(score.getImprovementTitle(), score.getFinalScore(), score.getRanking())).toList()).orElse(List.of());
        return new DashboardSummaryResponse(projectCount, applicationCount, feedbacks.size(), average, median, modes, min, max, deviation,
                sentimentCounts, sentimentPercentages, categoryCounts, monthly, issues, improvementStatuses, priorities);
    }

    private List<IssueHighlight> latestIssueGroups(UUID ownerId, UUID projectId, UUID applicationId, Set<UUID> feedbackIds) {
        UUID scopeProjectId = projectId;
        if (scopeProjectId == null && applicationId != null) scopeProjectId = applicationRepository.findById(applicationId).map(app -> app.getProject().getId()).orElse(null);
        UUID finalProjectId = scopeProjectId;
        return analysisRunRepository.findAllByRequestedByIdOrderByCreatedAtDesc(ownerId).stream()
                .filter(run -> applicationId == null || (run.getApplication() != null && run.getApplication().getId().equals(applicationId)) || (run.getProject() != null && run.getProject().getId().equals(finalProjectId)))
                .filter(run -> finalProjectId == null || (run.getProject() != null && run.getProject().getId().equals(finalProjectId)) || (run.getApplication() != null && run.getApplication().getProject().getId().equals(finalProjectId)))
                .findFirst().map(run -> issueGroupRepository.findAllByRunIdOrderByFeedbackCountDesc(run.getId()).stream().map(group -> {
                    long count = issueGroupItemRepository.findAllByIssueGroupId(group.getId()).stream().filter(item -> feedbackIds.contains(item.getFeedbackAnalysis().getFeedback().getId())).count();
                    return new IssueHighlight(group.getTitle(), group.getCategory().getName(), count);
                }).filter(group -> group.feedbackCount() > 0).limit(5).toList()).orElse(List.of());
    }

    private static Double median(List<Integer> values) {
        if (values.isEmpty()) return null; int middle = values.size() / 2;
        return values.size() % 2 == 1 ? values.get(middle).doubleValue() : (values.get(middle - 1) + values.get(middle)) / 2d;
    }
    private static List<Integer> modes(List<Integer> values) {
        if (values.isEmpty()) return List.of(); Map<Integer, Long> counts = new TreeMap<>(); values.forEach(value -> counts.merge(value, 1L, Long::sum));
        long max = counts.values().stream().mapToLong(Long::longValue).max().orElse(0);
        return counts.entrySet().stream().filter(entry -> entry.getValue() == max).map(Map.Entry::getKey).toList();
    }
    private static Double standardDeviation(List<Integer> values, Double average) {
        if (values.isEmpty()) return null; double variance = values.stream().mapToDouble(value -> Math.pow(value - average, 2)).sum() / values.size(); return Math.sqrt(variance);
    }
}
