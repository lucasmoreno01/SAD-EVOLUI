package com.sadevolui.modules.category.controller;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.sadevolui.modules.category.dto.CategoryRequest;
import com.sadevolui.modules.category.dto.CategoryResponse;
import com.sadevolui.modules.category.service.CategoryService;
import com.sadevolui.security.CurrentUser;

@RestController @RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) { this.categoryService = categoryService; }
    @GetMapping public List<CategoryResponse> list() { return categoryService.list(CurrentUser.id()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public CategoryResponse create(@Valid @RequestBody CategoryRequest request) { return categoryService.create(request, CurrentUser.id()); }
    @PutMapping("/{id}") public CategoryResponse update(@PathVariable UUID id, @Valid @RequestBody CategoryRequest request) { return categoryService.update(id, request, CurrentUser.id()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id) { categoryService.delete(id, CurrentUser.id()); }
}
