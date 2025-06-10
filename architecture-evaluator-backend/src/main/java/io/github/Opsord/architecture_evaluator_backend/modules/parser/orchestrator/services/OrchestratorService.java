package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.FileInstanceService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.AnalysedFileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.ProjectAnalysisInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.AnalysedClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.analysis.FileAnalysisService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.AnnotationType;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.PomScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.ScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.SrcScannerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorService.class);

    private final ScannerService scannerService;
    private final PomScannerService pomScannerService;
    private final SrcScannerService srcScannerService;
    private final FileInstanceService fileInstanceService;
    private final FileAnalysisService analysisService;

    /**
     * Orchestrates the project analysis by scanning the project and its pom.xml file, analyzing the file instances,
     * and organizing the results into a ProjectAnalysisInstance.
     *
     * @param projectPath The path to the project to be analyzed.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in the analysis.
     * @return A ProjectAnalysisInstance containing the organized analysis results.
     * @throws IOException If an error occurs during scanning or analysis.
     */
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

        // Filter out test classes
        List<FileInstance> fileInstancesWithoutTests = fileInstances.stream()
                .filter(unit -> unit.getFileAnnotations().stream()
                        .noneMatch(a -> a.equalsIgnoreCase(AnnotationType.SPRINGBOOT_TEST.getAnnotation())))
                .toList();

        PomFileDTO pomFileDTO = pomScannerService.scanPomFile(projectRoot);

        List<AnalysedFileInstance> analyzedUnits = analyzeCompilationUnits(
                fileInstances,
                fileInstancesWithoutTests,
                pomFileDTO,
                includeNonInternalDependencies
        );

        ProjectAnalysisInstance projectAnalysisInstance = organizeProjectAnalysis(analyzedUnits);
        projectAnalysisInstance.setProjectName(projectRoot.getName());
        projectAnalysisInstance.setPomFile(pomFileDTO);

        logger.info("Orchestration completed for project at path: {}", projectRoot.getAbsolutePath());
        return projectAnalysisInstance;
    }

    /**
     * Determines the internal base package from the given PomFileDTO.
     *
     * @param pomFileDTO The PomFileDTO containing the parsed information from the pom.xml file.
     * @return The internal base package as a String.
     */
    private String determineInternalBasePackage(PomFileDTO pomFileDTO) {
        return pomFileDTO.getGroupId();
    }

    /**
     * Analyzes a list of file instances and returns a list of AnalysedFileInstance.
     *
     * @param projectCompUnits The list of FileInstance to be analyzed.
     * @param projectCompUnitsWithoutTests The list of FileInstance without test classes.
     * @param pomFileDTO The PomFileDTO containing the parsed information from the pom.xml file.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in the analysis.
     * @return A list of AnalysedFileInstance containing the analyzed file instances.
     */
    private List<AnalysedFileInstance> analyzeCompilationUnits(
            List<FileInstance> projectCompUnits,
            List<FileInstance> projectCompUnitsWithoutTests,
            PomFileDTO pomFileDTO,
            boolean includeNonInternalDependencies
    ) {
        String internalBasePackage = determineInternalBasePackage(pomFileDTO);

        return projectCompUnits.stream()
                .map(compilationUnit -> createAnalysisDTO(
                        compilationUnit,
                        projectCompUnitsWithoutTests,
                        internalBasePackage,
                        pomFileDTO,
                        includeNonInternalDependencies
                ))
                .toList();
    }

    /**
     * Creates an AnalysedFileInstance containing the analysis of the given file instance.
     *
     * @param compilationUnit The FileInstance to be analyzed.
     * @param projectCompUnitsWithoutTests The list of FileInstance without test classes.
     * @param internalBasePackage The internal base package as a String.
     * @param pomFileDTO The PomFileDTO containing the parsed information from the pom.xml file.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in the analysis.
     * @return An AnalysedFileInstance containing the analysis.
     */
    private AnalysedFileInstance createAnalysisDTO(
            FileInstance compilationUnit,
            List<FileInstance> projectCompUnitsWithoutTests,
            String internalBasePackage,
            PomFileDTO pomFileDTO,
            boolean includeNonInternalDependencies
    ) {
        AnalysedClassInstance analysis = analysisService.analyseCompUnit(
                compilationUnit,
                projectCompUnitsWithoutTests,
                internalBasePackage,
                pomFileDTO,
                includeNonInternalDependencies
        );

        AnalysedFileInstance compUnitWithAnalysis = new AnalysedFileInstance();
        compUnitWithAnalysis.setFileInstance(compilationUnit);
        compUnitWithAnalysis.setAnalysedClassInstances(List.of(analysis));
        return compUnitWithAnalysis;
    }

    /**
     * Organizes the given list of AnalysedFileInstance into a ProjectAnalysisInstance.
     *
     * @param analysedFileInstances The list of AnalysedFileInstance to be organized.
     * @return A ProjectAnalysisInstance containing the organized file instances.
     */
    public ProjectAnalysisInstance organizeProjectAnalysis(List<AnalysedFileInstance> analysedFileInstances) {
        ProjectAnalysisInstance projectAnalysisInstance = new ProjectAnalysisInstance();
        projectAnalysisInstance.setEntities(filterAnalysedUnitByAnnotation(analysedFileInstances, AnnotationType.ENTITY));
        projectAnalysisInstance.setDocuments(filterAnalysedUnitByAnnotation(analysedFileInstances, AnnotationType.DOCUMENT));
        projectAnalysisInstance.setRepositories(filterAnalysedUnitByAnnotation(analysedFileInstances, AnnotationType.REPOSITORY));
        projectAnalysisInstance.setServices(filterAnalysedUnitByAnnotation(analysedFileInstances, AnnotationType.SERVICE));
        projectAnalysisInstance.setControllers(filterAnalysedUnitByAnnotation(analysedFileInstances, AnnotationType.CONTROLLER));
        projectAnalysisInstance.setTestClasses(filterAnalysedUnitByAnnotation(analysedFileInstances, AnnotationType.SPRINGBOOT_TEST));

        // Filter out units that don't have any of the AnnotationType annotations
        List<AnalysedFileInstance> otherClasses = analysedFileInstances.stream()
                .filter(dto -> dto.getFileInstance().getFileAnnotations().stream()
                        .noneMatch(annotation ->
                                Stream.of(AnnotationType.values())
                                        .anyMatch(type -> type.getAnnotation().equalsIgnoreCase(annotation))))
                .toList();
        projectAnalysisInstance.setOtherClasses(otherClasses);

        return projectAnalysisInstance;
    }

    /**
     * Filters the given list of AnalysedFileInstance by the specified annotation type.
     *
     * @param units The list of AnalysedFileInstance to filter.
     * @param annotationType The AnnotationType to filter by.
     * @return A filtered list of AnalysedFileInstance.
     */
    private List<AnalysedFileInstance> filterAnalysedUnitByAnnotation(
            List<AnalysedFileInstance> units,
            AnnotationType annotationType) {
        return units.stream()
                .filter(dto -> dto.getFileInstance().getFileAnnotations().stream()
                        .anyMatch(annotation -> annotation.equalsIgnoreCase(annotationType.getAnnotation())))
                .toList();
    }
}