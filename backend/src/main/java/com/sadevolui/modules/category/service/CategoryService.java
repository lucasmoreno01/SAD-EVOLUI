package com.sadevolui.modules.category.service;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sadevolui.modules.category.dto.CategoryRequest;
import com.sadevolui.modules.category.dto.CategoryResponse;
import com.sadevolui.modules.category.entity.Category;
import com.sadevolui.modules.category.repository.CategoryRepository;
import com.sadevolui.modules.user.repository.UserRepository;
import com.sadevolui.shared.exception.ConflictException;
import com.sadevolui.shared.exception.ResourceNotFoundException;

@Service @Transactional
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    public CategoryService(CategoryRepository categoryRepository, UserRepository userRepository) { this.categoryRepository = categoryRepository; this.userRepository = userRepository; }

    @Transactional(readOnly = true)
    public List<CategoryResponse> list(UUID ownerId) { return categoryRepository.findAvailableTo(ownerId).stream().map(CategoryService::toResponse).toList(); }

    public CategoryResponse create(CategoryRequest request, UUID ownerId) {
        String name = request.name().trim();
        if (categoryRepository.findByNameIgnoreCase(name).isPresent()) throw new ConflictException("Já existe uma categoria com esse nome.");
        var owner = userRepository.findById(ownerId).orElseThrow(() -> new ResourceNotFoundException("Usuário", ownerId));
        return toResponse(categoryRepository.save(new Category(owner, name, request.description())));
    }

    public CategoryResponse update(UUID id, CategoryRequest request, UUID ownerId) {
        Category category = findOwned(id, ownerId);
        if (categoryRepository.findByNameIgnoreCase(request.name().trim()).filter(existing -> !existing.getId().equals(id)).isPresent()) throw new ConflictException("Já existe uma categoria com esse nome.");
        category.update(request.name(), request.description());
        return toResponse(categoryRepository.save(category));
    }

    public void delete(UUID id, UUID ownerId) { categoryRepository.delete(findOwned(id, ownerId)); }

    private Category findOwned(UUID id, UUID ownerId) {
        return categoryRepository.findByIdAndOwnerId(id, ownerId).orElseThrow(() -> new ResourceNotFoundException("Categoria personalizada", id));
    }
    private static CategoryResponse toResponse(Category category) { return new CategoryResponse(category.getId(), category.getName(), category.getDescription(), category.getOwner() == null); }
}
