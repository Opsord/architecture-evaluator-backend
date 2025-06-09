package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import lombok.Data;

import java.util.List;

@Data
public class ProjectAnalysisInstance {
    private String projectPath;
    private List<AnalysedFileInstance> entities;
    private List<AnalysedFileInstance> documents;
    private List<AnalysedFileInstance> repositories;
    private List<AnalysedFileInstance> services;
    private List<AnalysedFileInstance> controllers;
    private List<AnalysedFileInstance> testClasses;
    private List<AnalysedFileInstance> otherClasses;
    private PomFileDTO pomFile;
}