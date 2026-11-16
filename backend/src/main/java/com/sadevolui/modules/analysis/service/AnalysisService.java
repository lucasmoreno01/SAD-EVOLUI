package com.sadevolui.modules.analysis.service;

import java.text.Normalizer;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sadevolui.modules.analysis.dto.AnalysisDtos.*;
import com.sadevolui.modules.analysis.dto.AnalysisRequest;
import com.sadevolui.modules.analysis.dto.ClassificationCorrectionRequest;
import com.sadevolui.modules.analysis.entity.AnalysisRun;
import com.sadevolui.modules.analysis.entity.ClassificationSource;
import com.sadevolui.modules.analysis.entity.FeedbackAnalysis;
import com.sadevolui.modules.analysis.entity.IssueGroup;
import com.sadevolui.modules.analysis.entity.IssueGroupItem;
import com.sadevolui.modules.analysis.entity.Sentiment;
import com.sadevolui.modules.analysis.repository.*;
import com.sadevolui.modules.application.entity.ManagedApplication;
import com.sadevolui.modules.application.repository.ApplicationRepository;
import com.sadevolui.modules.category.entity.Category;
import com.sadevolui.modules.category.repository.CategoryRepository;
import com.sadevolui.modules.feedback.entity.Feedback;
import com.sadevolui.modules.feedback.repository.FeedbackRepository;
import com.sadevolui.modules.project.entity.Project;
import com.sadevolui.modules.project.repository.ProjectRepository;
import com.sadevolui.modules.user.entity.UserAccount;
import com.sadevolui.modules.user.repository.UserRepository;
import com.sadevolui.shared.exception.BadRequestException;
import com.sadevolui.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class AnalysisService {
    private static final String METHOD_VERSION = "rating-keyword-jaccard-v1";
    private static final Pattern TOKEN_PATTERN = Pattern.compile("[^a-z0-9]+", Pattern.CASE_INSENSITIVE);
    private static final Set<String> STOP_WORDS = Set.of("para", "como", "mais", "muito", "essa", "esse", "isso", "com", "sem", "uma", "uns", "umas", "que", "por", "dos", "das", "the", "and", "for", "not", "app", "aplicativo", "aplicacao");
    private final AnalysisRunRepository runRepository;
    private final FeedbackAnalysisRepository analysisRepository;
    private final IssueGroupRepository groupRepository;
    private final IssueGroupItemRepository groupItemRepository;
    private final FeedbackRepository feedbackRepository;
    private final CategoryRepository categoryRepository;
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public AnalysisService(AnalysisRunRepository runRepository, FeedbackAnalysisRepository analysisRepository,
            IssueGroupRepository groupRepository, IssueGroupItemRepository groupItemRepository,
            FeedbackRepository feedbackRepository, CategoryRepository categoryRepository,
            ProjectRepository projectRepository, ApplicationRepository applicationRepository, UserRepository userRepository) {
        this.runRepository = runRepository; this.analysisRepository = analysisRepository;
        this.groupRepository = groupRepository; this.groupItemRepository = groupItemRepository;
        this.feedbackRepository = feedbackRepository; this.categoryRepository = categoryRepository;
        this.projectRepository = projectRepository; this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    public RunDetail run(AnalysisRequest request, UUID userId) {
        if (request.projectId() == null && request.applicationId() == null) throw new BadRequestException("Informe um projeto ou aplicativo para analisar.");
        Project project = request.projectId() == null ? null : projectRepository.findByIdAndOwnerId(request.projectId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto", request.projectId()));
        ManagedApplication application = request.applicationId() == null ? null : applicationRepository.findByIdAndProject_Owner_Id(request.applicationId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException("Aplicativo", request.applicationId()));
        if (project != null && application != null && !project.getId().equals(application.getProject().getId()))
            throw new BadRequestException("O aplicativo informado não pertence ao projeto selecionado.");
        List<Feedback> feedbacks;
        if (application != null) feedbacks = feedbackRepository.findAllByApplication_IdAndApplication_Project_Owner_IdOrderBySubmittedAtDesc(application.getId(), userId);
        else if (project != null) feedbacks = feedbackRepository.findAllByApplication_Project_IdAndApplication_Project_Owner_IdOrderBySubmittedAtDesc(project.getId(), userId);
        else feedbacks = feedbackRepository.findAllByApplication_Project_Owner_IdOrderBySubmittedAtDesc(userId);
        UserAccount user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
        AnalysisRun run = runRepository.save(new AnalysisRun(user, project, application, METHOD_VERSION));
        Map<String, Category> categories = new HashMap<>();
        categoryRepository.findAvailableTo(userId).forEach(category -> categories.put(normalize(category.getName()), category));
        List<FeedbackAnalysis> classified = feedbacks.stream().map(feedback -> new FeedbackAnalysis(run,
                feedback, sentiment(feedback.getRating()), categoryFor(feedback.getComment(), categories))).toList();
        classified = analysisRepository.saveAll(classified);
        persistGroups(run, classified);
        return detail(run, userId);
    }

    @Transactional(readOnly = true)
    public List<RunSummary> history(UUID userId) {
        return runRepository.findAllByRequestedByIdOrderByCreatedAtDesc(userId).stream().map(run -> new RunSummary(
                run.getId(), run.getProject() == null ? null : run.getProject().getId(),
                run.getApplication() == null ? null : run.getApplication().getId(), run.getMethodVersion(), run.getCreatedAt())).toList();
    }

    @Transactional(readOnly = true)
    public RunDetail get(UUID runId, UUID userId) {
        AnalysisRun run = findRun(runId, userId);
        return detail(run, userId);
    }

    public Classification correct(UUID analysisId, ClassificationCorrectionRequest request, UUID userId) {
        FeedbackAnalysis result = analysisRepository.findByIdAndRunRequestedById(analysisId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Classificação", analysisId));
        Category category = categoryRepository.findAvailableTo(userId).stream().filter(item -> item.getId().equals(request.categoryId())).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Categoria", request.categoryId()));
        UserAccount reviewer = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuário", userId));
        result.correct(request.sentiment(), category, reviewer);
        analysisRepository.save(result);
        return toClassification(result);
    }

    private AnalysisRun findRun(UUID id, UUID userId) {
        return runRepository.findByIdAndRequestedById(id, userId).orElseThrow(() -> new ResourceNotFoundException("Análise", id));
    }

    private RunDetail detail(AnalysisRun run, UUID userId) {
        List<FeedbackAnalysis> results = analysisRepository.findAllByRunId(run.getId());
        EnumMap<Sentiment, Long> sentimentCounts = new EnumMap<>(Sentiment.class);
        for (Sentiment sentiment : Sentiment.values()) sentimentCounts.put(sentiment, 0L);
        Map<String, Long> byCategory = new TreeMap<>();
        results.forEach(item -> {
            sentimentCounts.compute(item.getSentiment(), (key, value) -> value + 1);
            byCategory.merge(item.getCategory().getName(), 1L, Long::sum);
        });
        List<IssueGroup> groups = groupRepository.findAllByRunIdOrderByFeedbackCountDesc(run.getId());
        List<com.sadevolui.modules.analysis.dto.AnalysisDtos.IssueGroup> groupResponses = groups.stream().map(group -> {
            List<UUID> feedbackIds = groupItemRepository.findAllByIssueGroupId(group.getId()).stream()
                    .map(item -> item.getFeedbackAnalysis().getFeedback().getId()).toList();
            return new com.sadevolui.modules.analysis.dto.AnalysisDtos.IssueGroup(group.getId(), group.getTitle(),
                    group.getCategory().getId(), group.getCategory().getName(), group.getFeedbackCount(), feedbackIds);
        }).toList();
        return new RunDetail(run.getId(), run.getProject() == null ? null : run.getProject().getId(),
                run.getApplication() == null ? null : run.getApplication().getId(), run.getMethodVersion(), run.getCreatedAt(),
                results.size(), Map.of("POSITIVE", sentimentCounts.get(Sentiment.POSITIVE), "NEUTRAL", sentimentCounts.get(Sentiment.NEUTRAL), "NEGATIVE", sentimentCounts.get(Sentiment.NEGATIVE)),
                byCategory, results.stream().map(AnalysisService::toClassification).toList(), groupResponses,
                "Heurística versionada: sentimento inferido somente pela nota (1–2 negativo, 3 neutro, 4–5 positivo); tema por palavras-chave; grupos candidatos por similaridade Jaccard >= 0,40 dentro da mesma categoria. Não é modelo treinado; revise as classificações antes de decidir.");
    }

    private void persistGroups(AnalysisRun run, List<FeedbackAnalysis> results) {
        List<Cluster> clusters = new ArrayList<>();
        for (FeedbackAnalysis item : results) {
            Set<String> tokens = tokens(item.getFeedback().getComment());
            if (tokens.size() < 2) continue;
            Cluster match = clusters.stream().filter(cluster -> cluster.category.getId().equals(item.getCategory().getId()))
                    .max(Comparator.comparingDouble(cluster -> jaccard(tokens, cluster.representativeTokens)))
                    .filter(cluster -> jaccard(tokens, cluster.representativeTokens) >= 0.40).orElse(null);
            if (match == null) clusters.add(new Cluster(item.getCategory(), item, tokens)); else match.members.add(item);
        }
        for (Cluster cluster : clusters) {
            if (cluster.members.size() < 2) continue;
            String title = cluster.representative.getFeedback().getComment().trim();
            if (title.length() > 180) title = title.substring(0, 177) + "...";
            IssueGroup group = groupRepository.save(new IssueGroup(run, cluster.category, title, cluster.members.size()));
            groupItemRepository.saveAll(cluster.members.stream().map(item -> new IssueGroupItem(group, item)).toList());
        }
    }

    private static Classification toClassification(FeedbackAnalysis item) {
        return new Classification(item.getId(), item.getFeedback().getId(), item.getFeedback().getComment(), item.getFeedback().getRating(),
                item.getSentiment().name(), item.getCategory().getId(), item.getCategory().getName(), item.getClassificationSource().name(),
                item.getOriginalSentiment() == null ? null : item.getOriginalSentiment().name(),
                item.getOriginalCategory() == null ? null : item.getOriginalCategory().getId(),
                item.getOriginalCategory() == null ? null : item.getOriginalCategory().getName(), item.getReviewedAt());
    }

    private static Sentiment sentiment(int rating) { return rating <= 2 ? Sentiment.NEGATIVE : rating == 3 ? Sentiment.NEUTRAL : Sentiment.POSITIVE; }

    private static Category categoryFor(String comment, Map<String, Category> categories) {
        String text = normalize(comment);
        String[][] rules = {
            {"Desempenho", "lento", "lentidao", "demora", "rapido", "carregamento", "velocidade"},
            {"Autenticação", "login", "senha", "autenticacao", "acesso", "entrar", "cadastro", "sessao"},
            {"Estabilidade", "erro", "falha", "travamento", "trava", "fecha", "crash", "bug"},
            {"Navegação", "navegacao", "menu", "buscar", "busca", "encontrar", "fluxo"},
            {"Interface", "interface", "visual", "tela", "fonte", "cor", "layout", "botao"},
            {"Funcionalidades", "funcionalidade", "recurso", "adicionar", "incluir", "falta", "gostaria"}
        };
        for (String[] rule : rules) for (int index = 1; index < rule.length; index++) if (text.contains(rule[index])) {
            Category found = categories.get(normalize(rule[0]));
            if (found != null) return found;
        }
        return categories.get(normalize("Outros"));
    }

    private static Set<String> tokens(String comment) {
        Set<String> tokens = new HashSet<>();
        for (String token : TOKEN_PATTERN.split(normalize(comment))) if (token.length() > 2 && !STOP_WORDS.contains(token)) tokens.add(token);
        return tokens;
    }
    private static double jaccard(Set<String> left, Set<String> right) {
        Set<String> intersection = new HashSet<>(left); intersection.retainAll(right);
        Set<String> union = new HashSet<>(left); union.addAll(right);
        return union.isEmpty() ? 0 : (double) intersection.size() / union.size();
    }
    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }

    private static final class Cluster {
        private final Category category;
        private final FeedbackAnalysis representative;
        private final Set<String> representativeTokens;
        private final List<FeedbackAnalysis> members = new ArrayList<>();
        private Cluster(Category category, FeedbackAnalysis representative, Set<String> tokens) {
            this.category = category; this.representative = representative; this.representativeTokens = tokens; this.members.add(representative);
        }
    }
}
