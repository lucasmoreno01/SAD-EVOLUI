package com.sadevolui.modules.improvement.controller;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.sadevolui.modules.improvement.dto.ImprovementRequest;
import com.sadevolui.modules.improvement.dto.ImprovementResponse;
import com.sadevolui.modules.improvement.service.ImprovementService;
import com.sadevolui.security.CurrentUser;

@RestController @RequestMapping("/api/improvements")
public class ImprovementController {
    private final ImprovementService service;
    public ImprovementController(ImprovementService service) { this.service = service; }
    @GetMapping public List<ImprovementResponse> list() { return service.list(CurrentUser.id()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ImprovementResponse create(@Valid @RequestBody ImprovementRequest request) { return service.create(request, CurrentUser.id()); }
    @PutMapping("/{id}") public ImprovementResponse update(@PathVariable UUID id, @Valid @RequestBody ImprovementRequest request) { return service.update(id, request, CurrentUser.id()); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id) { service.delete(id, CurrentUser.id()); }
}
