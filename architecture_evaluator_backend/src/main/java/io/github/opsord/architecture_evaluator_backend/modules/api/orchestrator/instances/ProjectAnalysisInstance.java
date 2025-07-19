package io.github.opsord.architecture_evaluator_backend.modules.api.orchestrator.instances;

import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.ProcessedClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.gradle.GradleFileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.pom.PomFileInstance;
import lombok.Data;

import java.util.List;

@Data
public class ProjectAnalysisInstance {
    private String projectName;
    private List<ProcessedClassInstance> entities;
    private List<ProcessedClassInstance> documents;
    private List<ProcessedClassInstance> repositories;
    private List<ProcessedClassInstance> services;
    private List<ProcessedClassInstance> controllers;
    private List<ProcessedClassInstance> testClasses;
    private List<ProcessedClassInstance> otherClasses;
    private PomFileInstance pomFile;
    private GradleFileInstance gradleFile;
}