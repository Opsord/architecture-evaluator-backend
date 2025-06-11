package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.FileInstanceService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.ProcessedClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.FileAnalysisService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.AnnotationType;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.PomScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.ScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.SrcScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.ProjectAnalysisInstance;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorService.class);

    private final ScannerService scannerService;
    private final PomScannerService pomScannerService;
    private final SrcScannerService srcScannerService;
    private final FileAnalysisService analysisService;
    private final FileInstanceService fileInstanceService;

    public ProjectAnalysisInstance orchestrateProjectAnalysis(String projectPath, boolean includeNonInternalDependencies) throws IOException {
        logger.info("Starting orchestration for project at path: {}", projectPath);

        File projectRoot = scannerService.findProjectRoot(new File(projectPath));
        if (projectRoot == null) {
            logger.warn("Project root not found for path: {}", projectPath);
            throw new IOException("Project root not found");
        }

        List<File> srcFiles = srcScannerService.scanSrcFolder(new File(projectRoot, "src"));
        List<FileInstance> fileInstances = srcScannerService.parseJavaFiles(srcFiles, projectRoot);
        if (fileInstances.isEmpty()) {
            logger.warn("No Java files found in the project at path: {}", projectPath);
            throw new IOException("No Java files found in the project");
        }

        // Filter files without tests
        List<FileInstance> fileInstancesWithoutTests = fileInstances.stream()
                .filter(unit -> unit.getFileAnnotations().stream()
                        .noneMatch(a -> a.equalsIgnoreCase(AnnotationType.SPRINGBOOT_TEST.getAnnotation())))
                .toList();

        PomFileDTO pomFileDTO = pomScannerService.scanPomFile(projectRoot);

        // Populate class dependencies
        populateClassDependencies(fileInstancesWithoutTests);

        List<ProcessedClassInstance> processedClasses = analyzeCompilationUnits(
                fileInstances,
                pomFileDTO,
                includeNonInternalDependencies
        );

        ProjectAnalysisInstance projectAnalysisInstance = organizeProjectAnalysis(processedClasses);
        projectAnalysisInstance.setProjectName(projectRoot.getName());
        projectAnalysisInstance.setPomFile(pomFileDTO);

        logger.info("Orchestration completed for project at path: {}", projectRoot.getAbsolutePath());
        return projectAnalysisInstance;
    }

    private String determineInternalBasePackage(PomFileDTO pomFileDTO) {
        return pomFileDTO.getGroupId();
    }

    private List<ProcessedClassInstance> analyzeCompilationUnits(
            List<FileInstance> projectCompUnits,
            PomFileDTO pomFileDTO,
            boolean includeNonInternalDependencies
    ) {
        String internalBasePackage = determineInternalBasePackage(pomFileDTO);

        // Analyze all files and flatten the list of processed classes
        return projectCompUnits.stream()
                .flatMap(compilationUnit -> analysisService.analyseFileInstance(
                        compilationUnit,
                        internalBasePackage,
                        pomFileDTO
                ).stream())
                .toList();
    }

    public ProjectAnalysisInstance organizeProjectAnalysis(List<ProcessedClassInstance> processedClasses) {
        ProjectAnalysisInstance projectAnalysisInstance = new ProjectAnalysisInstance();
        projectAnalysisInstance.setEntities(filterByAnnotation(processedClasses, AnnotationType.ENTITY));
        projectAnalysisInstance.setDocuments(filterByAnnotation(processedClasses, AnnotationType.DOCUMENT));
        projectAnalysisInstance.setRepositories(filterByAnnotation(processedClasses, AnnotationType.REPOSITORY));
        projectAnalysisInstance.setServices(filterByAnnotation(processedClasses, AnnotationType.SERVICE));
        projectAnalysisInstance.setControllers(filterByAnnotation(processedClasses, AnnotationType.CONTROLLER));
        projectAnalysisInstance.setTestClasses(filterByAnnotation(processedClasses, AnnotationType.SPRINGBOOT_TEST));

        // Other classes without any of the above annotations
        List<ProcessedClassInstance> otherClasses = processedClasses.stream()
                .filter(pci -> pci.getClassInstance().getAnnotations().stream()
                        .noneMatch(annotation ->
                                List.of(
                                        AnnotationType.ENTITY.getAnnotation(),
                                        AnnotationType.DOCUMENT.getAnnotation(),
                                        AnnotationType.REPOSITORY.getAnnotation(),
                                        AnnotationType.SERVICE.getAnnotation(),
                                        AnnotationType.CONTROLLER.getAnnotation(),
                                        AnnotationType.SPRINGBOOT_TEST.getAnnotation()
                                ).contains(annotation)))
                .toList();
        projectAnalysisInstance.setOtherClasses(otherClasses);

        return projectAnalysisInstance;
    }

    private List<ProcessedClassInstance> filterByAnnotation(
            List<ProcessedClassInstance> classes,
            AnnotationType annotationType) {
        return classes.stream()
                .filter(pci -> pci.getClassInstance().getAnnotations().stream()
                        .anyMatch(annotation -> annotation.equalsIgnoreCase(annotationType.getAnnotation())))
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
                    .filter(other -> other.getClassDependencies() != null && other.getClassDependencies().contains(className))
                    .map(ClassInstance::getName)
                    .toList();
            cls.setDependentClasses(dependents);
        }
    }
}