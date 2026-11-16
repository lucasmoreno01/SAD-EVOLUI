package com.sadevolui.modules.decision.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import com.sadevolui.modules.analysis.repository.IssueGroupItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sadevolui.modules.decision.dto.DecisionDtos.*;
import com.sadevolui.modules.decision.dto.DecisionRequest;
import com.sadevolui.modules.decision.entity.DecisionRun;
import com.sadevolui.modules.decision.entity.DecisionScore;
import com.sadevolui.modules.decision.repository.DecisionRunRepository;
import com.sadevolui.modules.decision.repository.DecisionScoreRepository;
import com.sadevolui.modules.improvement.entity.Improvement;
import com.sadevolui.modules.improvement.repository.ImprovementRepository;
import com.sadevolui.modules.user.entity.UserAccount;
import com.sadevolui.modules.user.repository.UserRepository;
import com.sadevolui.shared.exception.BadRequestException;
import com.sadevolui.shared.exception.ResourceNotFoundException;

@Service @Transactional
public class DecisionService {
    private static final int SCALE = 8;
    private final DecisionRunRepository runRepository;
    private final DecisionScoreRepository scoreRepository;
    private final ImprovementRepository improvementRepository;
    private final IssueGroupItemRepository issueGroupItemRepository;
    private final UserRepository userRepository;

    public DecisionService(DecisionRunRepository runRepository, DecisionScoreRepository scoreRepository, ImprovementRepository improvementRepository, IssueGroupItemRepository issueGroupItemRepository, UserRepository userRepository) {
        this.runRepository = runRepository; this.scoreRepository = scoreRepository; this.improvementRepository = improvementRepository; this.issueGroupItemRepository = issueGroupItemRepository; this.userRepository = userRepository;
    }

    public Run calculate(DecisionRequest request, UUID userId) {
        BigDecimal total = request.impactWeight().add(request.urgencyWeight()).add(request.frequencyWeight()).add(request.effortWeight());
        if (total.signum() <= 0) throw new BadRequestException("Informe uma importância maior que zero para pelo menos um critério.");
        NormalizedWeights weights = normalizeWeights(request, total);
        if (new HashSet<>(request.improvementIds()).size() != request.improvementIds().size()) throw new BadRequestException("A lista contém melhorias repetidas.");
        List<Improvement> improvements = improvementRepository.findOwnedIds(request.improvementIds(), userId);
        if (improvements.size() != request.improvementIds().size()) throw new ResourceNotFoundException("Uma ou mais melhorias", null);
        UserAccount user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
        DecisionRun run = runRepository.save(new DecisionRun(request.name(), weights.impact(), weights.urgency(), weights.frequency(), weights.effort(), user));

        int maxImpact = improvements.stream().mapToInt(Improvement::getImpact).max().orElseThrow();
        int maxUrgency = improvements.stream().mapToInt(Improvement::getUrgency).max().orElseThrow();
        List<Candidate> candidates = improvements.stream().map(item -> new Candidate(item, frequencyCount(item))).toList();
        int maxFrequency = candidates.stream().mapToInt(Candidate::frequencyCount).max().orElseThrow();
        int minEffort = improvements.stream().mapToInt(Improvement::getEffort).min().orElseThrow();

        List<CalculatedScore> calculated = candidates.stream().map(candidate -> {
            Improvement item = candidate.improvement();
            BigDecimal impact = ratio(item.getImpact(), maxImpact);
            BigDecimal urgency = ratio(item.getUrgency(), maxUrgency);
            BigDecimal frequency = maxFrequency == 0 ? BigDecimal.ZERO.setScale(SCALE) : ratio(candidate.frequencyCount(), maxFrequency);
            BigDecimal effort = ratio(minEffort, item.getEffort());
            BigDecimal score = impact.multiply(weights.impact()).add(urgency.multiply(weights.urgency()))
                    .add(frequency.multiply(weights.frequency())).add(effort.multiply(weights.effort())).setScale(SCALE, RoundingMode.HALF_UP);
            return new CalculatedScore(item, candidate.frequencyCount(), impact, urgency, frequency, effort, score);
        }).sorted(Comparator.comparing(CalculatedScore::score).reversed().thenComparing(item -> item.improvement().getTitle(), String.CASE_INSENSITIVE_ORDER)).toList();

        List<DecisionScore> snapshots = new ArrayList<>();
        for (int index = 0; index < calculated.size(); index++) {
            CalculatedScore item = calculated.get(index);
            snapshots.add(new DecisionScore(run, item.improvement(), item.frequencyCount(), item.impact(), item.urgency(), item.frequency(), item.effort(), item.score(), index + 1));
        }
        scoreRepository.saveAll(snapshots);
        return detail(run);
    }

    @Transactional(readOnly = true)
    public List<RunSummary> history(UUID userId) {
        return runRepository.findAllByCreatedByIdOrderByCreatedAtDesc(userId).stream().map(run -> {
            List<DecisionScore> scores = scoreRepository.findAllByRunIdOrderByRankingAsc(run.getId());
            return new RunSummary(run.getId(), run.getName(), run.getCreatedAt(), scores.size(), scores.isEmpty() ? null : scores.get(0).getFinalScore());
        }).toList();
    }

    @Transactional(readOnly = true)
    public Run get(UUID id, UUID userId) {
        DecisionRun run = runRepository.findByIdAndCreatedById(id, userId).orElseThrow(() -> new ResourceNotFoundException("Decisão", id));
        return detail(run);
    }

    private Run detail(DecisionRun run) {
        com.sadevolui.modules.decision.dto.DecisionDtos.Weights weights = new com.sadevolui.modules.decision.dto.DecisionDtos.Weights(run.getImpactWeight(), run.getUrgencyWeight(), run.getFrequencyWeight(), run.getEffortWeight());
        List<Score> ranking = scoreRepository.findAllByRunIdOrderByRankingAsc(run.getId()).stream().map(score -> new Score(
                score.getImprovement().getId(), score.getImprovementTitle(), score.getImpactValue(), score.getUrgencyValue(), score.getFrequencyValue(), score.getEffortValue(),
                score.getNormalizedImpact(), score.getNormalizedUrgency(), score.getNormalizedFrequency(), score.getNormalizedEffort(), score.getFinalScore(), score.getRanking())).toList();
        String criteriaMeaning = "Impacto: gravidade e prejuízo à experiência. Urgência: necessidade temporal. "
                + ("LINKED_FEEDBACK_COUNT".equals(run.getFrequencyBasis())
                    ? "Frequência: contagem de avaliações pertencentes ao grupo de problemas associado e ao aplicativo da melhoria; não representa usuários distintos. Sem grupo, o valor é zero. "
                    : "Frequência: avaliação manual histórica em escala de 1 a 5. ")
                + "Esforço: complexidade e recursos estimados. Impacto, urgência e esforço são avaliados pelo gestor; a origem está registrada em cada melhoria.";
        return new Run(run.getId(), run.getName(), weights, run.getFrequencyBasis(), run.getCreatedAt(), ranking,
                "Benefício: valor da alternativa / maior valor; custo (esforço): menor valor / valor da alternativa.",
                criteriaMeaning);
    }

    private int frequencyCount(Improvement improvement) {
        if (improvement.getIssueGroup() == null) return 0;
        return Math.toIntExact(issueGroupItemRepository.countFeedbacksForApplication(improvement.getIssueGroup().getId(), improvement.getApplication().getId()));
    }

    private static BigDecimal ratio(int numerator, int denominator) {
        return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), SCALE, RoundingMode.HALF_UP);
    }
    private static NormalizedWeights normalizeWeights(DecisionRequest request, BigDecimal total) {
        BigDecimal impact = normalized(request.impactWeight(), total);
        BigDecimal urgency = normalized(request.urgencyWeight(), total);
        BigDecimal frequency = normalized(request.frequencyWeight(), total);
        BigDecimal effort = normalized(request.effortWeight(), total);
        BigDecimal difference = BigDecimal.ONE.subtract(impact.add(urgency).add(frequency).add(effort));
        if (request.impactWeight().compareTo(request.urgencyWeight()) >= 0
                && request.impactWeight().compareTo(request.frequencyWeight()) >= 0
                && request.impactWeight().compareTo(request.effortWeight()) >= 0) impact = impact.add(difference);
        else if (request.urgencyWeight().compareTo(request.frequencyWeight()) >= 0
                && request.urgencyWeight().compareTo(request.effortWeight()) >= 0) urgency = urgency.add(difference);
        else if (request.frequencyWeight().compareTo(request.effortWeight()) >= 0) frequency = frequency.add(difference);
        else effort = effort.add(difference);
        return new NormalizedWeights(impact, urgency, frequency, effort);
    }
    private static BigDecimal normalized(BigDecimal weight, BigDecimal total) {
        return weight.divide(total, 5, RoundingMode.HALF_UP);
    }
    private record NormalizedWeights(BigDecimal impact, BigDecimal urgency, BigDecimal frequency, BigDecimal effort) { }
    private record Candidate(Improvement improvement, int frequencyCount) { }
    private record CalculatedScore(Improvement improvement, int frequencyCount, BigDecimal impact, BigDecimal urgency, BigDecimal frequency, BigDecimal effort, BigDecimal score) { }
}
