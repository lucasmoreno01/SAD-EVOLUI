package com.sadevolui.modules.application.controller;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.sadevolui.modules.application.dto.ApplicationRequest;
import com.sadevolui.modules.application.dto.ApplicationResponse;
import com.sadevolui.modules.application.service.ApplicationService;
import com.sadevolui.security.CurrentUser;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {
    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public List<ApplicationResponse> list(@RequestParam(required = false) UUID projectId) {
        return applicationService.list(projectId, CurrentUser.id());
    }

    @GetMapping("/{id}")
    public ApplicationResponse get(@PathVariable UUID id) {
        return applicationService.get(id, CurrentUser.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse create(@Valid @RequestBody ApplicationRequest request) {
        return applicationService.create(request, CurrentUser.id());
    }

    @PutMapping("/{id}")
    public ApplicationResponse update(@PathVariable UUID id, @Valid @RequestBody ApplicationRequest request) {
        return applicationService.update(id, request, CurrentUser.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        applicationService.delete(id, CurrentUser.id());
    }
}
