package io.github.opsord.architecture_evaluator_backend.modules.parser.orchestrator.instances;

import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.ProcessedJavaTypeInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.instances.gradle.GradleFileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.project_scanner.instances.pom.PomFileInstance;
import lombok.Data;

import java.util.List;

@Data
public class ProjectAnalysisInstance {
    private String projectName;
    private List<ProcessedJavaTypeInstance> entities;
    private List<ProcessedJavaTypeInstance> documents;
    private List<ProcessedJavaTypeInstance> repositories;
    private List<ProcessedJavaTypeInstance> services;
    private List<ProcessedJavaTypeInstance> controllers;
    private List<ProcessedJavaTypeInstance> testClasses;
    private List<ProcessedJavaTypeInstance> otherClasses;
    private PomFileInstance pomFile;
    private GradleFileInstance gradleFile;
}