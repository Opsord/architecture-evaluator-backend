package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.services.CompilationUnitService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.AnalysedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.CompUnitWithAnalysisDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.ProjectAnalysisDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.summary.CompUnitSummaryDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.analysis.CompUnitAnalysisService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.summary.CompUnitSummaryService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.AnnotationType;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.PomScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.ScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.SrcScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
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
    private final CompilationUnitService compilationUnitService;
    private final CompUnitAnalysisService analysisService;
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
    public ProjectAnalysisDTO orchestrateProjectAnalysis(String projectPath, boolean includeNonInternalDependencies) throws IOException {
        logger.info("Starting orchestration for project at path: {}", projectPath);

        // Find the project root
        File projectRoot = scannerService.findProjectRoot(new File(projectPath));
        if (projectRoot == null) {
            logger.warn("Project root not found for path: {}", projectPath);
            throw new IOException("Project root not found");
        }

        // Scan the src folder for Java files
        List<File> srcFiles = srcScannerService.scanSrcFolder(new File(projectRoot, "src"));
        List<CustomCompilationUnitDTO> compilationUnits = srcScannerService.parseJavaFiles(srcFiles);

        // Filter out test classes
        List<CustomCompilationUnitDTO> compilationUnitsWithoutTests = compilationUnits.stream()
                .filter(unit -> unit.getAnnotations().stream().noneMatch(a -> a.getName().equalsIgnoreCase(AnnotationType.SPRINGBOOT_TEST.getAnnotation())))
                .toList();

        // Set the dependent classes for each compilation unit
        compilationUnitsWithoutTests.forEach(unit ->
                unit.setDependentClasses(
                        compilationUnitService.getDependentClasses(
                                unit.getClassName().isEmpty() ? "" : unit.getClassName().get(0),
                                compilationUnitsWithoutTests
                        )
                )
        );

        // Parse the pom.xml file
        PomFileDTO pomFileDTO = pomScannerService.scanPomFile(projectRoot);

        // Analyze the compilation units
        List<CompUnitWithAnalysisDTO> analyzedUnits = analyzeCompilationUnits(
                compilationUnits,
                compilationUnitsWithoutTests,
                pomFileDTO,
                includeNonInternalDependencies
        );

        // Organize the analyzed units into a ProjectAnalysisDTO
        ProjectAnalysisDTO projectAnalysisDTO = organizeProjectAnalysis(analyzedUnits);
        projectAnalysisDTO.setProjectPath(projectRoot.getAbsolutePath());
        projectAnalysisDTO.setPomFile(pomFileDTO);

        logger.info("Orchestration completed for project at path: {}", projectRoot.getAbsolutePath());
        return projectAnalysisDTO;
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
    private List<CompUnitWithAnalysisDTO> analyzeCompilationUnits(
            List<CustomCompilationUnitDTO> projectCompUnits,
            List<CustomCompilationUnitDTO> projectCompUnitsWithoutTests,
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
    private CompUnitWithAnalysisDTO createAnalysisDTO(
            CustomCompilationUnitDTO compilationUnit,
            List<CustomCompilationUnitDTO> projectCompUnitsWithoutTests,
            String internalBasePackage,
            PomFileDTO pomFileDTO,
            boolean includeNonInternalDependencies
    ) {

        // Generate the analysis
        AnalysedCompUnitDTO analysis = analysisService.analyseCompUnit(
                compilationUnit,
                projectCompUnitsWithoutTests,
                internalBasePackage,
                pomFileDTO,
                includeNonInternalDependencies
        );

        // Generate the summary
        CompUnitSummaryDTO summary = summaryService.createSummary(compilationUnit);

        // Create and return the DTO with both summary and analysis
        CompUnitWithAnalysisDTO compUnitWithAnalysis = new CompUnitWithAnalysisDTO();
        compUnitWithAnalysis.setCompUnitSummaryDTO(summary);
        compUnitWithAnalysis.setAnalysis(analysis);
        return compUnitWithAnalysis;
    }

    /**
     * Organizes the given list of CompUnitWithAnalysisDTO into a ProjectAnalysisDTO.
     *
     * @param compUnitWithAnalysisDTOS The list of CompUnitWithAnalysisDTO to be organized.
     * @return A ProjectAnalysisDTO containing the organized compilation units.
     */
    public ProjectAnalysisDTO organizeProjectAnalysis(List<CompUnitWithAnalysisDTO> compUnitWithAnalysisDTOS) {
        ProjectAnalysisDTO projectAnalysisDTO = new ProjectAnalysisDTO();
        projectAnalysisDTO.setEntities(filterAnalysedUnitByAnnotation(compUnitWithAnalysisDTOS, AnnotationType.ENTITY));
        projectAnalysisDTO.setDocuments(filterAnalysedUnitByAnnotation(compUnitWithAnalysisDTOS, AnnotationType.DOCUMENT));
        projectAnalysisDTO.setRepositories(filterAnalysedUnitByAnnotation(compUnitWithAnalysisDTOS, AnnotationType.REPOSITORY));
        projectAnalysisDTO.setServices(filterAnalysedUnitByAnnotation(compUnitWithAnalysisDTOS, AnnotationType.SERVICE));
        projectAnalysisDTO.setControllers(filterAnalysedUnitByAnnotation(compUnitWithAnalysisDTOS, AnnotationType.CONTROLLER));
        projectAnalysisDTO.setTestClasses(filterAnalysedUnitByAnnotation(compUnitWithAnalysisDTOS, AnnotationType.SPRINGBOOT_TEST));

        // Filter out units that don't have any of the AnnotationType annotations
        List<CompUnitWithAnalysisDTO> otherClasses = compUnitWithAnalysisDTOS.stream()
                .filter(dto -> dto.getCompUnitSummaryDTO().getAnnotationDTOS().stream()
                        .noneMatch(annotation ->
                                Stream.of(AnnotationType.values())
                                        .anyMatch(type -> type.getAnnotation().equalsIgnoreCase(annotation.getName()))))
                .toList();
        projectAnalysisDTO.setOtherClasses(otherClasses);

        return projectAnalysisDTO;
    }

    private List<CompUnitWithAnalysisDTO> filterAnalysedUnitByAnnotation(
            List<CompUnitWithAnalysisDTO> units,
            AnnotationType annotationType) {
        return units.stream()
                .filter(dto -> {
                    // Check if the analysis contains the annotation category
                    return dto.getCompUnitSummaryDTO().getAnnotationDTOS().stream()
                            .anyMatch(category -> category.getName().equalsIgnoreCase(annotationType.getAnnotation()));
                })
                .toList();
    }
}