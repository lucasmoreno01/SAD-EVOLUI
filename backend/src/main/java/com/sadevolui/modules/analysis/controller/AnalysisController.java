package com.sadevolui.modules.analysis.controller;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.sadevolui.modules.analysis.dto.AnalysisDtos.Classification;
import com.sadevolui.modules.analysis.dto.AnalysisDtos.RunDetail;
import com.sadevolui.modules.analysis.dto.AnalysisDtos.RunSummary;
import com.sadevolui.modules.analysis.dto.AnalysisRequest;
import com.sadevolui.modules.analysis.dto.ClassificationCorrectionRequest;
import com.sadevolui.modules.analysis.service.AnalysisService;
import com.sadevolui.security.CurrentUser;

@RestController
@RequestMapping("/api/analyses")
public class AnalysisController {
    private final AnalysisService analysisService;
    public AnalysisController(AnalysisService analysisService) { this.analysisService = analysisService; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RunDetail run(@RequestBody AnalysisRequest request) { return analysisService.run(request, CurrentUser.id()); }

    @GetMapping
    public List<RunSummary> history() { return analysisService.history(CurrentUser.id()); }

    @GetMapping("/{id}")
    public RunDetail get(@PathVariable UUID id) { return analysisService.get(id, CurrentUser.id()); }

    @PatchMapping("/classifications/{id}")
    public Classification correct(@PathVariable UUID id, @Valid @RequestBody ClassificationCorrectionRequest request) {
        return analysisService.correct(id, request, CurrentUser.id());
    }
}
