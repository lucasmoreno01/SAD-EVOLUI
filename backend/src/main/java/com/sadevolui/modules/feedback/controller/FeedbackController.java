package com.sadevolui.modules.feedback.controller;

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
import com.sadevolui.modules.feedback.dto.FeedbackRequest;
import com.sadevolui.modules.feedback.dto.FeedbackResponse;
import com.sadevolui.modules.feedback.service.FeedbackService;
import com.sadevolui.security.CurrentUser;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {
    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping
    public List<FeedbackResponse> list(@RequestParam(required = false) UUID applicationId) {
        return feedbackService.list(applicationId, CurrentUser.id());
    }

    @GetMapping("/{id}")
    public FeedbackResponse get(@PathVariable UUID id) {
        return feedbackService.get(id, CurrentUser.id());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackResponse create(@Valid @RequestBody FeedbackRequest request) {
        return feedbackService.create(request, CurrentUser.id());
    }

    @PutMapping("/{id}")
    public FeedbackResponse update(@PathVariable UUID id, @Valid @RequestBody FeedbackRequest request) {
        return feedbackService.update(id, request, CurrentUser.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        feedbackService.delete(id, CurrentUser.id());
    }
}
