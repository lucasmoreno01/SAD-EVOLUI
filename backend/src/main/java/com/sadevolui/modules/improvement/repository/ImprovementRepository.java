package com.sadevolui.modules.improvement.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sadevolui.modules.improvement.entity.Improvement;

public interface ImprovementRepository extends JpaRepository<Improvement, UUID> {
    @Query("select i from Improvement i where i.application.project.owner.id = :ownerId order by i.createdAt desc")
    List<Improvement> findAllOwned(@Param("ownerId") UUID ownerId);
    Optional<Improvement> findByIdAndApplication_Project_Owner_Id(UUID id, UUID ownerId);
    @Query("select i from Improvement i where i.id in :ids and i.application.project.owner.id = :ownerId")
    List<Improvement> findOwnedIds(@Param("ids") List<UUID> ids, @Param("ownerId") UUID ownerId);
    @Query("select count(i) from Improvement i where i.application.project.owner.id = :ownerId")
    long countOwned(@Param("ownerId") UUID ownerId);
    long countByStatusAndApplication_Project_Owner_Id(com.sadevolui.modules.improvement.entity.ImprovementStatus status, UUID ownerId);
}
