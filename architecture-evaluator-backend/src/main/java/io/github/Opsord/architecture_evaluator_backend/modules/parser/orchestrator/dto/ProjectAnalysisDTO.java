package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ProjectAnalysisDTO {
    private String projectPath;
    private List<CompUnitWithAnalysisDTO> entities;
    private List<CompUnitWithAnalysisDTO> documents;
    private List<CompUnitWithAnalysisDTO> repositories;
    private List<CompUnitWithAnalysisDTO> services;
    private List<CompUnitWithAnalysisDTO> controllers;
    private List<CompUnitWithAnalysisDTO> testClasses;
    private List<CompUnitWithAnalysisDTO> otherClasses;
    private PomFileDTO pomFile;
}