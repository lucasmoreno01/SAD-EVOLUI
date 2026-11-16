package com.sadevolui.modules.decision.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import com.sadevolui.modules.analysis.entity.IssueGroup;
import com.sadevolui.modules.analysis.repository.IssueGroupItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.sadevolui.modules.application.entity.ManagedApplication;
import com.sadevolui.modules.decision.dto.DecisionRequest;
import com.sadevolui.modules.decision.entity.DecisionScore;
import com.sadevolui.modules.decision.repository.DecisionRunRepository;
import com.sadevolui.modules.decision.repository.DecisionScoreRepository;
import com.sadevolui.modules.improvement.entity.Improvement;
import com.sadevolui.modules.improvement.entity.ImprovementStatus;
import com.sadevolui.modules.improvement.repository.ImprovementRepository;
import com.sadevolui.modules.project.entity.Project;
import com.sadevolui.modules.user.entity.UserAccount;
import com.sadevolui.modules.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {
    @Mock DecisionRunRepository runRepository;
    @Mock DecisionScoreRepository scoreRepository;
    @Mock ImprovementRepository improvementRepository;
    @Mock IssueGroupItemRepository issueGroupItemRepository;
    @Mock UserRepository userRepository;
    private DecisionService service;

    @BeforeEach
    void setUp() {
        service = new DecisionService(runRepository, scoreRepository, improvementRepository, issueGroupItemRepository, userRepository);
    }

    @Test
    void normalizesBenefitAndCostAndSortsByWeightedScore() {
        UUID userId = UUID.randomUUID();
        UserAccount user = new UserAccount("Gestora", "gestora@example.com", "hashed-password");
        Project project = new Project(user, "Projeto", null);
        ManagedApplication application = new ManagedApplication(project, "App", null);
        IssueGroup firstGroup = org.mockito.Mockito.mock(IssueGroup.class);
        IssueGroup secondGroup = org.mockito.Mockito.mock(IssueGroup.class);
        when(firstGroup.getId()).thenReturn(UUID.randomUUID());
        when(secondGroup.getId()).thenReturn(UUID.randomUUID());
        Improvement first = new Improvement(application, firstGroup, "A", "Primeira", 5, 4, 5, "Avaliação manual", ImprovementStatus.PROPOSED, user);
        Improvement second = new Improvement(application, secondGroup, "B", "Segunda", 3, 5, 2, "Avaliação manual", ImprovementStatus.PROPOSED, user);
        List<Improvement> alternatives = List.of(first, second);
        AtomicReference<List<DecisionScore>> savedScores = new AtomicReference<>(List.of());
        when(improvementRepository.findOwnedIds(anyList(), any())).thenReturn(alternatives);
        when(issueGroupItemRepository.countFeedbacksForApplication(any(), any())).thenReturn(2L, 5L);
        when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
        when(runRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(scoreRepository.saveAll(anyList())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked") List<DecisionScore> saved = invocation.getArgument(0);
            savedScores.set(saved);
            return saved;
        });
        when(scoreRepository.findAllByRunIdOrderByRankingAsc(any())).thenAnswer(invocation -> savedScores.get());

        var result = service.calculate(new DecisionRequest("Rodada", decimal("4"), decimal("2"), decimal("2"), decimal("2"),
                List.of(UUID.randomUUID(), UUID.randomUUID())), userId);

        assertThat(result.ranking()).extracting(score -> score.title()).containsExactly("B", "A");
        assertThat(result.ranking().get(0).finalScore()).isEqualByComparingTo("0.84000000");
        assertThat(result.ranking().get(1).normalizedEffort()).isEqualByComparingTo("0.40000000");
        assertThat(result.weights().impact()).isEqualByComparingTo("0.40000");
        assertThat(result.weights().urgency()).isEqualByComparingTo("0.20000");
        assertThat(result.weights().frequency()).isEqualByComparingTo("0.20000");
        assertThat(result.weights().effort()).isEqualByComparingTo("0.20000");
        assertThat(result.frequencyBasis()).isEqualTo("LINKED_FEEDBACK_COUNT");
        assertThat(result.ranking().get(0).frequency()).isEqualTo(5);
        assertThat(result.ranking().get(1).frequency()).isEqualTo(2);
    }

    private static BigDecimal decimal(String value) { return new BigDecimal(value); }
}
