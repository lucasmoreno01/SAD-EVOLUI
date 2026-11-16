package com.sadevolui.modules.category.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.sadevolui.modules.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByNameIgnoreCase(String name);
    java.util.List<Category> findAllByOrderByNameAsc();
    @Query("select c from Category c where c.owner is null or c.owner.id = :ownerId order by c.name")
    java.util.List<Category> findAvailableTo(@Param("ownerId") UUID ownerId);
    Optional<Category> findByIdAndOwnerId(UUID id, UUID ownerId);
}
