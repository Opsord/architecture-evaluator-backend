// backend/architecture-evaluator-backend/src/main/java/io/github/opsord/architecture_evaluator_backend/modules/parser/orchestrator/services/OrchestratorService.java
package io.github.opsord.architecture_evaluator_backend.modules.api.orchestrator.services;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.LayerAnnotation;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.file_instance.FileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.parser.services.file_instance.FileInstanceService;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.ProcessedClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.FileAnalysisService;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.pom.PomFileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.instances.gradle.GradleFileInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.services.PomScannerService;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.services.GradleScannerService;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.services.ScannerService;
import io.github.opsord.architecture_evaluator_backend.modules.api.project_scanner.services.SrcScannerService;
import io.github.opsord.architecture_evaluator_backend.modules.api.orchestrator.instances.ProjectAnalysisInstance;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorService.class);

    private final ScannerService scannerService;
    private final PomScannerService pomScannerService;
    private final GradleScannerService gradleScannerService;
    private final SrcScannerService srcScannerService;
    private final FileAnalysisService analysisService;
    private final FileInstanceService fileInstanceService;

    public ProjectAnalysisInstance orchestrateProjectAnalysis(String projectPath) throws IOException {
        logger.info("Starting orchestration for project");

        File projectRoot = scannerService.findProjectRoot(new File(projectPath));
        if (projectRoot == null) {
            logger.warn("Project root not found");
            throw new IOException("Project root not found");
        }

        List<File> srcFiles = srcScannerService.scanSrcFolder(new File(projectRoot, "src"));
        List<FileInstance> fileInstances = srcScannerService.parseJavaFiles(srcFiles, projectRoot);
        if (fileInstances.isEmpty()) {
            logger.warn("No Java files found in the project");
            throw new IOException("No Java files found in the project");
        }

        // Filter files without tests
        List<FileInstance> fileInstancesWithoutTests = fileInstances.stream()
                .filter(fi -> !fi.getClasses().isEmpty() && fi.getClasses().stream()
                        .noneMatch(cls -> cls.getLayerAnnotation() == LayerAnnotation.TESTING))
                .toList();

        Optional<PomFileInstance> pomFileInstance = pomScannerService.scanPomFile(projectRoot);
        Optional<GradleFileInstance> gradleFileInstance = gradleScannerService.scanGradleFile(projectRoot);

        // Populate class dependencies
        populateClassDependencies(fileInstancesWithoutTests);

        List<ProcessedClassInstance> processedClasses = analyzeCompilationUnits(
                fileInstances,
                pomFileInstance,
                gradleFileInstance);

        ProjectAnalysisInstance projectAnalysisInstance = organizeProjectAnalysis(processedClasses);
        projectAnalysisInstance.setProjectName(projectRoot.getName());
        pomFileInstance.ifPresent(projectAnalysisInstance::setPomFile);
        gradleFileInstance.ifPresent(projectAnalysisInstance::setGradleFile);

        logger.info("Orchestration completed for project at path: {}", projectRoot.getAbsolutePath());
        return projectAnalysisInstance;
    }

    private String determineInternalBasePackage(Optional<PomFileInstance> pomFileInstance, Optional<GradleFileInstance> gradleFileInstance) {
        if (pomFileInstance.isPresent()) {
            return pomFileInstance.get().getGroupId();
        } else if (gradleFileInstance.isPresent()) {
            return gradleFileInstance.get().getGroup();
        } else {
            return "io.github.opsord.architecture_evaluator_backend";
        }
    }

    private List<ProcessedClassInstance> analyzeCompilationUnits(
            List<FileInstance> projectCompUnits,
            Optional<PomFileInstance> pomFileInstance,
            Optional<GradleFileInstance> gradleFileInstance) {
        String internalBasePackage = determineInternalBasePackage(pomFileInstance, gradleFileInstance);

        // Analyze all files and flatten the list of processed classes
        return projectCompUnits.stream()
                .flatMap(compilationUnit -> analysisService.analyseFileInstance(
                        compilationUnit,
                        internalBasePackage,
                        pomFileInstance).stream())
                .toList();
    }

    public ProjectAnalysisInstance organizeProjectAnalysis(List<ProcessedClassInstance> processedClasses) {
        ProjectAnalysisInstance projectAnalysisInstance = new ProjectAnalysisInstance();
        projectAnalysisInstance.setEntities(filterByLayerAnnotation(processedClasses, LayerAnnotation.ENTITY));
        projectAnalysisInstance.setDocuments(filterByLayerAnnotation(processedClasses, LayerAnnotation.DOCUMENT));
        projectAnalysisInstance.setRepositories(filterByLayerAnnotation(processedClasses, LayerAnnotation.REPOSITORY));
        projectAnalysisInstance.setServices(filterByLayerAnnotation(processedClasses, LayerAnnotation.SERVICE));
        projectAnalysisInstance.setControllers(filterByLayerAnnotation(processedClasses, LayerAnnotation.CONTROLLER));
        projectAnalysisInstance.setTestClasses(filterByLayerAnnotation(processedClasses, LayerAnnotation.TESTING));
        // Other classes without any of the above annotations
        List<ProcessedClassInstance> otherClasses = processedClasses.stream()
                .filter(pci -> pci.getClassInstance().getLayerAnnotation() == LayerAnnotation.OTHER
                        || pci.getClassInstance().getLayerAnnotation() == LayerAnnotation.UNKNOWN)
                .toList();
        projectAnalysisInstance.setOtherClasses(otherClasses);

        return projectAnalysisInstance;
    }

    private List<ProcessedClassInstance> filterByLayerAnnotation(
            List<ProcessedClassInstance> classes,
            LayerAnnotation layerAnnotation) {
        return classes.stream()
                .filter(pci -> pci.getClassInstance().getLayerAnnotation() == layerAnnotation)
                .toList();
    }

    public void populateClassDependencies(List<FileInstance> allFiles) {
        // 1. Set classDependencies for each class
        List<ClassInstance> allClasses = allFiles.stream()
                .flatMap(f -> f.getClasses().stream())
                .toList();

        for (ClassInstance cls : allClasses) {
            List<String> dependencies = fileInstanceService.getDependentClassNamesFromClass(cls, allFiles);
            cls.setClassDependencies(dependencies);
        }

        // 2. Set dependentClasses for each class
        for (ClassInstance cls : allClasses) {
            String className = cls.getName();
            List<String> dependents = allClasses.stream()
                    .filter(other -> other.getClassDependencies() != null
                            && other.getClassDependencies().contains(className))
                    .map(ClassInstance::getName)
                    .toList();
            cls.setDependentClasses(dependents);
        }
    }
}