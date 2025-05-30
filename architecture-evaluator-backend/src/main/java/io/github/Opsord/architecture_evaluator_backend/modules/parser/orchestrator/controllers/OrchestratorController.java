package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.controllers;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.ProjectAnalysisDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.services.FileManagerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.services.OrchestratorService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/orchestrator")
@RequiredArgsConstructor
public class OrchestratorController {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorController.class);
    private final OrchestratorService orchestratorService;
    private final FileManagerService fileManagerService;

    @PostMapping("/analyze")
    public ResponseEntity<ProjectAnalysisDTO> analyzeProject(@RequestParam String projectPath,
                                                             @RequestParam(defaultValue = "false") boolean includeNonInternalDependencies) {
        try {
            logger.info("Received request to analyze project at path: {}", projectPath);
            ProjectAnalysisDTO result = orchestratorService.orchestrateProjectAnalysis(projectPath, includeNonInternalDependencies);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("Error analyzing project at path: {}", projectPath, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/analyze-upload")
    public ResponseEntity<ProjectAnalysisDTO> analyzeProject(@RequestParam("project") MultipartFile projectFolder,
                                                             @RequestParam(defaultValue = "false") boolean includeNonInternalDependencies) {
        File tempDir = null;
        try {
            logger.info("Received request to analyze project from uploaded folder");

            // Save the uploaded file
            File uploadedFile = fileManagerService.saveUploadedFile(projectFolder);
            tempDir = uploadedFile.getParentFile();

            // Extract the archive
            File extractedDir = fileManagerService.extractArchive(uploadedFile);

            // Call the orchestration service
            ProjectAnalysisDTO result = orchestratorService.orchestrateProjectAnalysis(extractedDir.getAbsolutePath(), includeNonInternalDependencies);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("Error analyzing uploaded project", e);
            return ResponseEntity.internalServerError().build();
        } finally {
            if (tempDir != null) {
                fileManagerService.deleteRecursively(tempDir);
            }
        }
    }

    @PostMapping("/analyze-github")
    public ResponseEntity<ProjectAnalysisDTO> analyzeGitHubRepo(@RequestParam String repoUrl,
                                                                @RequestParam(defaultValue = "false") boolean includeNonInternalDependencies) {
        File tempDir = null;
        try {
            logger.info("Received request to analyze GitHub repository: {}", repoUrl);

            // Download the repository as a ZIP file
            File zipFile = fileManagerService.downloadGitHubRepository(repoUrl);
            tempDir = zipFile.getParentFile();

            // Extract the ZIP file
            File extractedDir = fileManagerService.extractArchive(zipFile);

            // Analyze the extracted project
            ProjectAnalysisDTO result = orchestratorService.orchestrateProjectAnalysis(extractedDir.getPath(), includeNonInternalDependencies);

            return ResponseEntity.ok(result);
        } catch (IOException ioException) {
            logger.error("Error analyzing GitHub repository", ioException);
            return ResponseEntity.internalServerError().build();
        } finally {
            if (tempDir != null) {
                fileManagerService.deleteRecursively(tempDir);
            }
        }
    }
}