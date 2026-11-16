package com.sadevolui.modules.project.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sadevolui.modules.project.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, UUID> {
    java.util.List<Project> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId);
    java.util.Optional<Project> findByIdAndOwnerId(UUID id, UUID ownerId);
    long countByOwnerId(UUID ownerId);
}
