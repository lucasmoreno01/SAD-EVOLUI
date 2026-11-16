package com.sadevolui.modules.application.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.sadevolui.modules.application.entity.ManagedApplication;

public interface ApplicationRepository extends JpaRepository<ManagedApplication, UUID> {
    List<ManagedApplication> findAllByProject_Owner_IdOrderByCreatedAtDesc(UUID ownerId);
    List<ManagedApplication> findAllByProject_IdAndProject_Owner_IdOrderByCreatedAtDesc(UUID projectId, UUID ownerId);
    java.util.Optional<ManagedApplication> findByIdAndProject_Owner_Id(UUID id, UUID ownerId);
    boolean existsByProjectId(UUID projectId);
    long countByProject_Owner_Id(UUID ownerId);
    long countByProject_IdAndProject_Owner_Id(UUID projectId, UUID ownerId);
}
