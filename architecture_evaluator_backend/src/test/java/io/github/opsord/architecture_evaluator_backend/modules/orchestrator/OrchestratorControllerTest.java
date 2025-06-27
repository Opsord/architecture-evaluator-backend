// File: OrchestratorControllerTest.java
package io.github.opsord.architecture_evaluator_backend.modules.orchestrator;

import io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.controllers.OrchestratorController;
import io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.instances.ProjectAnalysisInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.services.FileManagerService;
import io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.services.OrchestratorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.io.IOException;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrchestratorController.class)
class OrchestratorControllerTest {

    @MockBean
    private OrchestratorService orchestratorService;

    @MockBean
    private FileManagerService fileManagerService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void analyzeProject_shouldReturnOk() throws Exception {
        ProjectAnalysisInstance mockResult = new ProjectAnalysisInstance();
        Mockito.when(orchestratorService.orchestrateProjectAnalysis(anyString()))
                .thenReturn(mockResult);

        mockMvc.perform(post("/api/orchestrator/analyze")
                        .param("projectPath", "some/path"))
                .andExpect(status().isOk());
    }

    @Test
    void analyzeProjectUpload_shouldReturnOk() throws Exception {
        ProjectAnalysisInstance mockResult = new ProjectAnalysisInstance();
        File tempFile = File.createTempFile("test", ".zip");
        File extractedDir = tempFile.getParentFile();

        Mockito.when(fileManagerService.saveUploadedFile(any())).thenReturn(tempFile);
        Mockito.when(fileManagerService.extractArchive(any())).thenReturn(extractedDir);
        Mockito.when(orchestratorService.orchestrateProjectAnalysis(anyString()))
                .thenReturn(mockResult);

        MockMultipartFile multipartFile = new MockMultipartFile(
                "project", "test.zip", "application/zip", "dummy".getBytes());

        mockMvc.perform(multipart("/api/orchestrator/analyze-upload")
                        .file(multipartFile))
                .andExpect(status().isOk());
    }

    @Test
    void analyzeGitHubRepo_shouldReturnOk() throws Exception {
        ProjectAnalysisInstance mockResult = new ProjectAnalysisInstance();
        File zipFile = File.createTempFile("repo", ".zip");
        File extractedDir = zipFile.getParentFile();

        Mockito.when(fileManagerService.downloadGitHubRepository(anyString())).thenReturn(zipFile);
        Mockito.when(fileManagerService.extractArchive(any())).thenReturn(extractedDir);
        Mockito.when(orchestratorService.orchestrateProjectAnalysis(anyString()))
                .thenReturn(mockResult);

        mockMvc.perform(post("/api/orchestrator/analyze-github")
                        .param("repoUrl", "https://github.com/user/repo"))
                .andExpect(status().isOk());
    }

    // File: OrchestratorControllerTest.java
    @Test
    void analyzeProject_shouldReturnInternalServerError_onException() throws Exception {
        Mockito.when(orchestratorService.orchestrateProjectAnalysis(anyString()))
                .thenThrow(new IOException("Test exception"));

        mockMvc.perform(post("/api/orchestrator/analyze")
                        .param("projectPath", "some/path"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void analyzeProjectUpload_shouldReturnInternalServerError_onException() throws Exception {
        Mockito.when(fileManagerService.saveUploadedFile(any()))
                .thenThrow(new IOException("Test exception"));

        MockMultipartFile multipartFile = new MockMultipartFile(
                "project", "test.zip", "application/zip", "dummy".getBytes());

        mockMvc.perform(multipart("/api/orchestrator/analyze-upload")
                        .file(multipartFile))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void analyzeGitHubRepo_shouldReturnInternalServerError_onException() throws Exception {
        Mockito.when(fileManagerService.downloadGitHubRepository(anyString()))
                .thenThrow(new IOException("Test exception"));

        mockMvc.perform(post("/api/orchestrator/analyze-github")
                        .param("repoUrl", "https://github.com/user/repo"))
                .andExpect(status().isInternalServerError());
    }
}