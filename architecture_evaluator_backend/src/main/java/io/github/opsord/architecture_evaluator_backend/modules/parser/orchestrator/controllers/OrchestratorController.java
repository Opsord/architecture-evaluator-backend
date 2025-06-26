package io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.controllers;

import io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.instances.ProjectAnalysisInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.services.FileManagerService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.services.OrchestratorService;
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
    public ResponseEntity<ProjectAnalysisInstance> analyzeProject(@RequestParam String projectPath) {
        try {
            logger.info("Received request to analyze project locally");
            ProjectAnalysisInstance result = orchestratorService.orchestrateProjectAnalysis(projectPath);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.error("Error analyzing project at path: {}", projectPath, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/analyze-upload")
    public ResponseEntity<ProjectAnalysisInstance> analyzeProject(@RequestParam("project") MultipartFile projectFolder) {
        File tempDir = null;
        try {
            logger.info("Received request to analyze project from uploaded folder");

            // Save the uploaded file
            File uploadedFile = fileManagerService.saveUploadedFile(projectFolder);
            tempDir = uploadedFile.getParentFile();

            // Extract the archive
            File extractedDir = fileManagerService.extractArchive(uploadedFile);

            // Call the orchestration service
            ProjectAnalysisInstance result = orchestratorService
                    .orchestrateProjectAnalysis(extractedDir.getAbsolutePath());

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
    public ResponseEntity<ProjectAnalysisInstance> analyzeGitHubRepo(@RequestParam String repoUrl) {
        File tempDir = null;
        try {
            logger.info("Received request to analyze GitHub repository");

            // Download the repository as a ZIP file
            File zipFile = fileManagerService.downloadGitHubRepository(repoUrl);
            tempDir = zipFile.getParentFile();

            // Extract the ZIP file
            File extractedDir = fileManagerService.extractArchive(zipFile);

            // Analyze the extracted project
            ProjectAnalysisInstance result = orchestratorService.orchestrateProjectAnalysis(extractedDir.getPath());

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