package com.sadevolui.modules.application.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sadevolui.modules.application.dto.ApplicationRequest;
import com.sadevolui.modules.application.dto.ApplicationResponse;
import com.sadevolui.modules.application.entity.ManagedApplication;
import com.sadevolui.modules.application.repository.ApplicationRepository;
import com.sadevolui.modules.feedback.repository.FeedbackRepository;
import com.sadevolui.modules.project.entity.Project;
import com.sadevolui.modules.project.repository.ProjectRepository;
import com.sadevolui.shared.exception.ConflictException;
import com.sadevolui.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;
    private final FeedbackRepository feedbackRepository;

    public ApplicationService(ApplicationRepository applicationRepository, ProjectRepository projectRepository, FeedbackRepository feedbackRepository) {
        this.applicationRepository = applicationRepository;
        this.projectRepository = projectRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> list(UUID projectId, UUID ownerId) {
        List<ManagedApplication> applications = projectId == null
                ? applicationRepository.findAllByProject_Owner_IdOrderByCreatedAtDesc(ownerId)
                : applicationRepository.findAllByProject_IdAndProject_Owner_IdOrderByCreatedAtDesc(projectId, ownerId);
        return applications.stream().map(ApplicationService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationResponse get(UUID id, UUID ownerId) {
        return toResponse(findApplication(id, ownerId));
    }

    public ApplicationResponse create(ApplicationRequest request, UUID ownerId) {
        Project project = findProject(request.projectId(), ownerId);
        return toResponse(applicationRepository.save(new ManagedApplication(project, request.name(), request.description())));
    }

    public ApplicationResponse update(UUID id, ApplicationRequest request, UUID ownerId) {
        ManagedApplication application = findApplication(id, ownerId);
        application.update(findProject(request.projectId(), ownerId), request.name(), request.description());
        return toResponse(applicationRepository.save(application));
    }

    public void delete(UUID id, UUID ownerId) {
        ManagedApplication application = findApplication(id, ownerId);
        if (feedbackRepository.existsByApplicationId(id)) {
            throw new ConflictException("Remova ou transfira os feedbacks vinculados antes de excluir o aplicativo.");
        }
        applicationRepository.delete(application);
    }

    private ManagedApplication findApplication(UUID id, UUID ownerId) {
        return applicationRepository.findByIdAndProject_Owner_Id(id, ownerId).orElseThrow(() -> new ResourceNotFoundException("Aplicativo", id));
    }

    private Project findProject(UUID id, UUID ownerId) {
        return projectRepository.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private static ApplicationResponse toResponse(ManagedApplication application) {
        return new ApplicationResponse(application.getId(), application.getProject().getId(), application.getProject().getName(), application.getName(), application.getDescription(), application.getCreatedAt(), application.getUpdatedAt());
    }
}
