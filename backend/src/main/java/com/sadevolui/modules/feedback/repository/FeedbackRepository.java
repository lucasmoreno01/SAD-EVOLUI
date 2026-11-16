package com.sadevolui.modules.feedback.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sadevolui.modules.feedback.entity.Feedback;

public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    List<Feedback> findAllByApplication_Project_Owner_IdOrderBySubmittedAtDesc(UUID ownerId);
    List<Feedback> findAllByApplication_IdAndApplication_Project_Owner_IdOrderBySubmittedAtDesc(UUID applicationId, UUID ownerId);
    List<Feedback> findAllByApplication_Project_IdAndApplication_Project_Owner_IdOrderBySubmittedAtDesc(UUID projectId, UUID ownerId);
    java.util.Optional<Feedback> findByIdAndApplication_Project_Owner_Id(UUID id, UUID ownerId);
    boolean existsByApplicationId(UUID applicationId);
    boolean existsByApplicationIdAndRatingAndCommentAndSubmittedAt(UUID applicationId, short rating, String comment, Instant submittedAt);

    @Query("select avg(f.rating) from Feedback f")
    Double findAverageRating();

    @Query("select count(f) from Feedback f where f.application.project.owner.id = :ownerId")
    long countByOwnerId(UUID ownerId);

    @Query("select avg(f.rating) from Feedback f where f.application.project.owner.id = :ownerId")
    Double findAverageRatingByOwnerId(UUID ownerId);

    @Query("select f from Feedback f where f.application.project.owner.id = :ownerId and (:projectId is null or f.application.project.id = :projectId) and (:applicationId is null or f.application.id = :applicationId) and f.submittedAt >= :fromAt and f.submittedAt < :toExclusive order by f.submittedAt")
    List<Feedback> findForDashboard(@Param("ownerId") UUID ownerId, @Param("projectId") UUID projectId,
            @Param("applicationId") UUID applicationId, @Param("fromAt") Instant fromAt, @Param("toExclusive") Instant toExclusive);
}
