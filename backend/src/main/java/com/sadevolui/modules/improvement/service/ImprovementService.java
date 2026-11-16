package com.sadevolui.modules.improvement.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sadevolui.modules.analysis.entity.IssueGroup;
import com.sadevolui.modules.analysis.repository.IssueGroupRepository;
import com.sadevolui.modules.analysis.repository.IssueGroupItemRepository;
import com.sadevolui.modules.application.entity.ManagedApplication;
import com.sadevolui.modules.application.repository.ApplicationRepository;
import com.sadevolui.modules.improvement.dto.ImprovementRequest;
import com.sadevolui.modules.improvement.dto.ImprovementResponse;
import com.sadevolui.modules.improvement.entity.Improvement;
import com.sadevolui.modules.improvement.repository.ImprovementRepository;
import com.sadevolui.modules.user.entity.UserAccount;
import com.sadevolui.modules.user.repository.UserRepository;
import com.sadevolui.shared.exception.ResourceNotFoundException;

@Service @Transactional
public class ImprovementService {
    private final ImprovementRepository improvementRepository;
    private final ApplicationRepository applicationRepository;
    private final IssueGroupRepository issueGroupRepository;
    private final IssueGroupItemRepository issueGroupItemRepository;
    private final UserRepository userRepository;
    public ImprovementService(ImprovementRepository improvementRepository, ApplicationRepository applicationRepository, IssueGroupRepository issueGroupRepository, IssueGroupItemRepository issueGroupItemRepository, UserRepository userRepository) {
        this.improvementRepository = improvementRepository; this.applicationRepository = applicationRepository; this.issueGroupRepository = issueGroupRepository; this.issueGroupItemRepository = issueGroupItemRepository; this.userRepository = userRepository;
    }
    @Transactional(readOnly = true) public List<ImprovementResponse> list(UUID ownerId) { return improvementRepository.findAllOwned(ownerId).stream().map(this::toResponse).toList(); }
    public ImprovementResponse create(ImprovementRequest request, UUID ownerId) {
        ManagedApplication application = applicationRepository.findByIdAndProject_Owner_Id(request.applicationId(), ownerId).orElseThrow(() -> new ResourceNotFoundException("Aplicativo", request.applicationId()));
        IssueGroup group = findGroup(request.issueGroupId(), ownerId, application);
        UserAccount user = userRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Usuário", ownerId));
        return toResponse(improvementRepository.save(new Improvement(application, group, request.title(), request.description(), request.impact(), request.urgency(), request.effort(), request.assessmentOrigin(), request.status(), user)));
    }
    public ImprovementResponse update(UUID id, ImprovementRequest request, UUID ownerId) {
        Improvement item = findOwned(id, ownerId);
        ManagedApplication application = applicationRepository.findByIdAndProject_Owner_Id(request.applicationId(), ownerId).orElseThrow(() -> new ResourceNotFoundException("Aplicativo", request.applicationId()));
        IssueGroup group = findGroup(request.issueGroupId(), ownerId, application);
        item.update(application, group, request.title(), request.description(), request.impact(), request.urgency(), request.effort(), request.assessmentOrigin(), request.status());
        return toResponse(improvementRepository.save(item));
    }
    public void delete(UUID id, UUID ownerId) { improvementRepository.delete(findOwned(id, ownerId)); }
    private Improvement findOwned(UUID id, UUID ownerId) { return improvementRepository.findByIdAndApplication_Project_Owner_Id(id, ownerId).orElseThrow(() -> new ResourceNotFoundException("Melhoria", id)); }
    private IssueGroup findGroup(UUID groupId, UUID ownerId, ManagedApplication application) {
        if (groupId == null) return null;
        IssueGroup group = issueGroupRepository.findByIdAndRunRequestedById(groupId, ownerId).orElseThrow(() -> new ResourceNotFoundException("Grupo de problemas", groupId));
        UUID groupProjectId = group.getRun().getProject() != null ? group.getRun().getProject().getId() : group.getRun().getApplication().getProject().getId();
        if (!groupProjectId.equals(application.getProject().getId())) throw new ResourceNotFoundException("Grupo de problemas", groupId);
        return group;
    }
    private ImprovementResponse toResponse(Improvement item) {
        IssueGroup group = item.getIssueGroup();
        int frequency = group == null ? 0 : Math.toIntExact(issueGroupItemRepository.countFeedbacksForApplication(group.getId(), item.getApplication().getId()));
        return new ImprovementResponse(item.getId(), item.getApplication().getId(), item.getApplication().getName(), item.getApplication().getProject().getName(),
                group == null ? null : group.getId(), group == null ? null : group.getTitle(), item.getTitle(), item.getDescription(),
                item.getImpact(), item.getUrgency(), frequency, item.getEffort(), item.getAssessmentOrigin(), item.getStatus(), item.getCreatedAt(), item.getUpdatedAt());
    }
}
