package com.sadevolui.modules.decision.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
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
    private final UserRepository userRepository;

    public DecisionService(DecisionRunRepository runRepository, DecisionScoreRepository scoreRepository, ImprovementRepository improvementRepository, UserRepository userRepository) {
        this.runRepository = runRepository; this.scoreRepository = scoreRepository; this.improvementRepository = improvementRepository; this.userRepository = userRepository;
    }

    public Run calculate(DecisionRequest request, UUID userId) {
        BigDecimal total = request.impactWeight().add(request.urgencyWeight()).add(request.frequencyWeight()).add(request.effortWeight());
        if (total.compareTo(BigDecimal.ONE) != 0) throw new BadRequestException("A soma dos pesos deve ser exatamente 1,00.");
        if (new HashSet<>(request.improvementIds()).size() != request.improvementIds().size()) throw new BadRequestException("A lista contém melhorias repetidas.");
        List<Improvement> improvements = improvementRepository.findOwnedIds(request.improvementIds(), userId);
        if (improvements.size() != request.improvementIds().size()) throw new ResourceNotFoundException("Uma ou mais melhorias", null);
        UserAccount user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
        DecisionRun run = runRepository.save(new DecisionRun(request.name(), request.impactWeight(), request.urgencyWeight(), request.frequencyWeight(), request.effortWeight(), user));

        int maxImpact = improvements.stream().mapToInt(Improvement::getImpact).max().orElseThrow();
        int maxUrgency = improvements.stream().mapToInt(Improvement::getUrgency).max().orElseThrow();
        int maxFrequency = improvements.stream().mapToInt(Improvement::getFrequency).max().orElseThrow();
        int minEffort = improvements.stream().mapToInt(Improvement::getEffort).min().orElseThrow();

        List<CalculatedScore> calculated = improvements.stream().map(item -> {
            BigDecimal impact = ratio(item.getImpact(), maxImpact);
            BigDecimal urgency = ratio(item.getUrgency(), maxUrgency);
            BigDecimal frequency = ratio(item.getFrequency(), maxFrequency);
            BigDecimal effort = ratio(minEffort, item.getEffort());
            BigDecimal score = impact.multiply(request.impactWeight()).add(urgency.multiply(request.urgencyWeight()))
                    .add(frequency.multiply(request.frequencyWeight())).add(effort.multiply(request.effortWeight())).setScale(SCALE, RoundingMode.HALF_UP);
            return new CalculatedScore(item, impact, urgency, frequency, effort, score);
        }).sorted(Comparator.comparing(CalculatedScore::score).reversed().thenComparing(item -> item.improvement().getTitle(), String.CASE_INSENSITIVE_ORDER)).toList();

        List<DecisionScore> snapshots = new ArrayList<>();
        for (int index = 0; index < calculated.size(); index++) {
            CalculatedScore item = calculated.get(index);
            snapshots.add(new DecisionScore(run, item.improvement(), item.impact(), item.urgency(), item.frequency(), item.effort(), item.score(), index + 1));
        }
        scoreRepository.saveAll(snapshots);
        return detail(run, request);
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
        return detail(run, null);
    }

    private Run detail(DecisionRun run, DecisionRequest request) {
        Weights weights = request == null
                ? new Weights(run.getImpactWeight(), run.getUrgencyWeight(), run.getFrequencyWeight(), run.getEffortWeight())
                : new Weights(request.impactWeight(), request.urgencyWeight(), request.frequencyWeight(), request.effortWeight());
        List<Score> ranking = scoreRepository.findAllByRunIdOrderByRankingAsc(run.getId()).stream().map(score -> new Score(
                score.getImprovement().getId(), score.getImprovementTitle(), score.getImpactValue(), score.getUrgencyValue(), score.getFrequencyValue(), score.getEffortValue(),
                score.getNormalizedImpact(), score.getNormalizedUrgency(), score.getNormalizedFrequency(), score.getNormalizedEffort(), score.getFinalScore(), score.getRanking())).toList();
        return new Run(run.getId(), run.getName(), weights, run.getCreatedAt(), ranking,
                "Benefício: valor da alternativa / maior valor; custo (esforço): menor valor / valor da alternativa.",
                "Impacto: gravidade e prejuízo à experiência. Urgência: necessidade temporal. Frequência: recorrência nos feedbacks (não representa usuários distintos). Esforço: complexidade e recursos estimados. Escala de 1 a 5 informada pelo gestor; origem registrada em cada melhoria.");
    }

    private static BigDecimal ratio(int numerator, int denominator) {
        return BigDecimal.valueOf(numerator).divide(BigDecimal.valueOf(denominator), SCALE, RoundingMode.HALF_UP);
    }
    private record CalculatedScore(Improvement improvement, BigDecimal impact, BigDecimal urgency, BigDecimal frequency, BigDecimal effort, BigDecimal score) { }
}
