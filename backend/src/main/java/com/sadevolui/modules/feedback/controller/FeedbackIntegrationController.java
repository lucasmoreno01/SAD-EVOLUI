package com.sadevolui.modules.feedback.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.sadevolui.modules.feedback.dto.FeedbackRequest;
import com.sadevolui.modules.feedback.dto.FeedbackResponse;
import com.sadevolui.modules.feedback.service.FeedbackService;
import com.sadevolui.security.IntegrationApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api/integrations/feedbacks")
public class FeedbackIntegrationController {
    private final FeedbackService feedbackService;
    private final IntegrationApiKeyService apiKeyService;

    public FeedbackIntegrationController(FeedbackService feedbackService, IntegrationApiKeyService apiKeyService) {
        this.feedbackService = feedbackService;
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Recebe uma avaliação de uma integração externa",
            description = "Registra a avaliação vinculada ao aplicativo informado. Requer a chave configurada no servidor.",
            security = @SecurityRequirement(name = "integrationApiKey"))
    public FeedbackResponse receive(
            @RequestHeader(name = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody FeedbackRequest request) {
        apiKeyService.requireValid(apiKey);
        return feedbackService.createExternal(request);
    }
}
