package com.sadevolui.modules.project.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sadevolui.modules.application.repository.ApplicationRepository;
import com.sadevolui.modules.project.dto.ProjectRequest;
import com.sadevolui.modules.project.dto.ProjectResponse;
import com.sadevolui.modules.project.entity.Project;
import com.sadevolui.modules.project.repository.ProjectRepository;
import com.sadevolui.modules.user.entity.UserAccount;
import com.sadevolui.modules.user.repository.UserRepository;
import com.sadevolui.shared.exception.ConflictException;
import com.sadevolui.shared.exception.ResourceNotFoundException;

@Service
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository, ApplicationRepository applicationRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> list(UUID ownerId) {
        return projectRepository.findAllByOwnerIdOrderByCreatedAtDesc(ownerId).stream().map(ProjectService::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse get(UUID id, UUID ownerId) {
        return toResponse(findProject(id, ownerId));
    }

    public ProjectResponse create(ProjectRequest request, UUID ownerId) {
        UserAccount owner = userRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Usuário", ownerId));
        return toResponse(projectRepository.save(new Project(owner, request.name(), request.description())));
    }

    public ProjectResponse update(UUID id, ProjectRequest request, UUID ownerId) {
        Project project = findProject(id, ownerId);
        project.update(request.name(), request.description());
        return toResponse(projectRepository.save(project));
    }

    public void delete(UUID id, UUID ownerId) {
        Project project = findProject(id, ownerId);
        if (applicationRepository.existsByProjectId(id)) {
            throw new ConflictException("Remova ou transfira os aplicativos vinculados antes de excluir o projeto.");
        }
        projectRepository.delete(project);
    }

    private Project findProject(UUID id, UUID ownerId) {
        return projectRepository.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new ResourceNotFoundException("Projeto", id));
    }

    private static ProjectResponse toResponse(Project project) {
        return new ProjectResponse(project.getId(), project.getName(), project.getDescription(), project.getCreatedAt(), project.getUpdatedAt());
    }
}
