package com.sadevolui.modules.decision.controller;

import java.util.List;
import java.util.UUID;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.sadevolui.modules.decision.dto.DecisionDtos.Run;
import com.sadevolui.modules.decision.dto.DecisionDtos.RunSummary;
import com.sadevolui.modules.decision.dto.DecisionRequest;
import com.sadevolui.modules.decision.service.DecisionService;
import com.sadevolui.security.CurrentUser;

@RestController @RequestMapping("/api/decisions")
public class DecisionController {
    private final DecisionService service;
    public DecisionController(DecisionService service) { this.service = service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Run calculate(@Valid @RequestBody DecisionRequest request) { return service.calculate(request, CurrentUser.id()); }
    @GetMapping public List<RunSummary> history() { return service.history(CurrentUser.id()); }
    @GetMapping("/{id}") public Run get(@PathVariable UUID id) { return service.get(id, CurrentUser.id()); }
}
