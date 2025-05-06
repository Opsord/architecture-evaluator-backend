package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.controllers;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.services.OrchestratorService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.SummarizedCompUnitDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/orchestrator")
@RequiredArgsConstructor
public class OrchestratorController {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorController.class);
    private final OrchestratorService orchestratorService;

    @PostMapping("/analyze")
    public ResponseEntity<List<SummarizedCompUnitDTO>> analyzeProject(@RequestParam String projectPath) {
        try {
            logger.info("Received request to analyze project at path: {}", projectPath);
            // Convert the map values to a list
            List<SummarizedCompUnitDTO> result = orchestratorService.orchestrateProjectAnalysis(projectPath)
                    .values()
                    .stream()
                    .toList();
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("Error analyzing project at path: {}", projectPath, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
