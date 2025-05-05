package io.github.Opsord.architecture_evaluator_backend.modules.parser.project_manager.controllers;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_manager.dto.ProjectDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_manager.services.ProjectScannerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/project-scanner")
@RequiredArgsConstructor
public class ProjectScannerController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectScannerController.class);
    private final ProjectScannerService projectScannerService;

    @PostMapping("/scan")
    public ResponseEntity<ProjectDTO> scanProject(@RequestParam String projectPath) {
        try {
            logger.info("Received request to scan project at path: {}", projectPath);
            ProjectDTO result = projectScannerService.scanAndOrganizeProject(projectPath);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("Error scanning project at path: {}", projectPath, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}