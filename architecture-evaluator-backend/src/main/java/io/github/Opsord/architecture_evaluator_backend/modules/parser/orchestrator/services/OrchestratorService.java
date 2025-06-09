package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.file_instance.FileInstanceService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.AnalysedFileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.ProjectAnalysisInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.AnalysedClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.summary.FileInstanceSummary;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.analysis.FileAnalysisService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.summary.CompUnitSummaryService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.AnnotationType;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.PomScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.ScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.SrcScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.CustomCompilationUnit;
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
    private final CompUnitSummaryService summaryService;

    /**
     * Orchestrates the project analysis by scanning the project and its pom.xml file, analyzing the compilation units,
     * and organizing the results into a ProjectAnalysisDTO.
     *
     * @param projectPath The path to the project to be analyzed.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in the analysis.
     * @return A ProjectAnalysisDTO containing the organized analysis results.
     * @throws IOException If an error occurs during scanning or analysis.
     */
    public ProjectAnalysisInstance orchestrateProjectAnalysis(String projectPath, boolean includeNonInternalDependencies) throws IOException {
        logger.info("Starting orchestration for project at path: {}", projectPath);

        // Find the project root
        File projectRoot = scannerService.findProjectRoot(new File(projectPath));
        if (projectRoot == null) {
            logger.warn("Project root not found for path: {}", projectPath);
            throw new IOException("Project root not found");
        }

        // Scan the src folder for Java files
        List<File> srcFiles = srcScannerService.scanSrcFolder(new File(projectRoot, "src"));
        List<CustomCompilationUnit> compilationUnits = srcScannerService.parseJavaFiles(srcFiles);

        // Filter out test classes
        List<CustomCompilationUnit> compilationUnitsWithoutTests = compilationUnits.stream()
                .filter(unit -> unit.getAnnotations().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AnnotationType.SPRINGBOOT_TEST.getAnnotation())))
                .toList();

        // Set the dependent classes for each compilation unit
        compilationUnitsWithoutTests.forEach(unit ->
                unit.setDependentClasses(
                        fileInstanceService.getDependentClassesFromFile(
                                unit.getClasses().isEmpty() ? "" : unit.getClasses().get(0),
                                compilationUnitsWithoutTests
                        )
                )
        );

        // Parse the pom.xml file
        PomFileDTO pomFileDTO = pomScannerService.scanPomFile(projectRoot);

        // Analyze the compilation units
        List<AnalysedFileInstance> analyzedUnits = analyzeCompilationUnits(
                compilationUnits,
                compilationUnitsWithoutTests,
                pomFileDTO,
                includeNonInternalDependencies
        );

        // Organize the analyzed units into a ProjectAnalysisDTO
        ProjectAnalysisInstance projectAnalysisInstance = organizeProjectAnalysis(analyzedUnits);
        projectAnalysisInstance.setProjectPath(projectRoot.getAbsolutePath());
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
     * Analyzes a list of compilation units and returns a list of CompUnitWithAnalysisDTO.
     *
     * @param projectCompUnits The list of CustomCompilationUnitDTOs to be analyzed.
     * @param pomFileDTO The PomFileDTO containing the parsed information from the pom.xml file.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in the analysis.
     * @return A list of CompUnitWithAnalysisDTO containing the analyzed compilation units.
     */
    private List<AnalysedFileInstance> analyzeCompilationUnits(
            List<CustomCompilationUnit> projectCompUnits,
            List<CustomCompilationUnit> projectCompUnitsWithoutTests,
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
     * Creates a CompUnitWithAnalysisDTO containing the summary and analysis of the given compilation unit.
     *
     * @param compilationUnit The CustomCompilationUnitDTO to be analyzed.
     * @param internalBasePackage The internal base package as a String.
     * @param pomFileDTO The PomFileDTO containing the parsed information from the pom.xml file.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in the analysis.
     * @return A CompUnitWithAnalysisDTO containing the summary and analysis.
     */
    private AnalysedFileInstance createAnalysisDTO(
            CustomCompilationUnit compilationUnit,
            List<CustomCompilationUnit> projectCompUnitsWithoutTests,
            String internalBasePackage,
            PomFileDTO pomFileDTO,
            boolean includeNonInternalDependencies
    ) {

        // Generate the analysis
        AnalysedClassInstance analysis = analysisService.analyseCompUnit(
                compilationUnit,
                projectCompUnitsWithoutTests,
                internalBasePackage,
                pomFileDTO,
                includeNonInternalDependencies
        );

        // Generate the summary
        FileInstanceSummary summary = summaryService.createSummary(compilationUnit);

        // Create and return the DTO with both summary and analysis
        AnalysedFileInstance compUnitWithAnalysis = new AnalysedFileInstance();
        compUnitWithAnalysis.setFileInstance(summary);
        compUnitWithAnalysis.setAnalysis(analysis);
        return compUnitWithAnalysis;
    }

    /**
     * Organizes the given list of CompUnitWithAnalysisDTO into a ProjectAnalysisDTO.
     *
     * @param analysedFileInstances The list of CompUnitWithAnalysisDTO to be organized.
     * @return A ProjectAnalysisDTO containing the organized compilation units.
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
                .filter(dto -> dto.getFileInstance().getAnnotationDTOS().stream()
                        .noneMatch(annotation ->
                                Stream.of(AnnotationType.values())
                                        .anyMatch(type -> type.getAnnotation().equalsIgnoreCase(annotation.getName()))))
                .toList();
        projectAnalysisInstance.setOtherClasses(otherClasses);

        return projectAnalysisInstance;
    }

    private List<AnalysedFileInstance> filterAnalysedUnitByAnnotation(
            List<AnalysedFileInstance> units,
            AnnotationType annotationType) {
        return units.stream()
                .filter(dto -> {
                    // Check if the analysis contains the annotation category
                    return dto.getFileInstance().getAnnotationDTOS().stream()
                            .anyMatch(category -> category.getName().equalsIgnoreCase(annotationType.getAnnotation()));
                })
                .toList();
    }
}