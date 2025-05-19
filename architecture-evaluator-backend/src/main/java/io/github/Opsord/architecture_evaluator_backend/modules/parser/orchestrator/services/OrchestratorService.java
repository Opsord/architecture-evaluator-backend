package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.AnalysedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.CompUnitWithAnalysisDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.ProjectAnalysisDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.CCUSummarizingService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.AnnotationType;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.ProjectDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.parts.PomService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.services.parts.ProjectService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorService.class);

    private final PomService pomService;
    private final ProjectService projectService;
    private final CCUSummarizingService summarizingService;

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

        ProjectDTO projectDTO = scanProject(projectPath);
        PomFileDTO pomFileDTO = scanPomFile(projectPath);
        // Extract all compilation units from the project
        List<CustomCompilationUnitDTO> compilationUnits = extractCompilationUnits(projectDTO);
        // Generate the compilation units without tests
        List<CustomCompilationUnitDTO> compilationUnitsWithoutTests = extractCompilationUnitsWithoutTests(projectDTO);
        // Analyze the compilation units
        List<CompUnitWithAnalysisDTO> analyzedUnits = analyzeCompilationUnits(
                compilationUnits,
                compilationUnitsWithoutTests,
                pomFileDTO,
                includeNonInternalDependencies);
        // Organize the analysis results into a ProjectAnalysisDTO
        ProjectAnalysisDTO projectAnalysisDTO = organizeProjectAnalysis(analyzedUnits);
        projectAnalysisDTO.setProjectPath(projectPath);
        projectAnalysisDTO.setPomFile(pomFileDTO);

        logger.info("Orchestration completed for project at path: {}", projectPath);
        return projectAnalysisDTO;
    }

    /**
     * Scans the project at the given path and returns a ProjectDTO containing the scanned compilation units.
     *
     * @param projectPath The path to the project to be scanned.
     * @return A ProjectDTO containing the scanned compilation units.
     * @throws IOException If an error occurs during scanning.
     */
    private ProjectDTO scanProject(String projectPath) throws IOException {
        return projectService.scanProjectToDTO(projectPath);
    }

    /**
     * Scans the pom.xml file at the given path and returns a PomFileDTO containing the parsed information.
     *
     * @param projectPath The path to the project to be scanned.
     * @return A PomFileDTO containing the parsed information from the pom.xml file.
     */
    private PomFileDTO scanPomFile(String projectPath) {
        PomFileDTO pomFileDTO = pomService.scanPomFile(projectPath);
        if (pomFileDTO == null) {
            throw new IllegalStateException("Failed to scan pom.xml file at path: " + projectPath);
        }
        return pomFileDTO;
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
     * Extracts all compilation units from a ProjectDTO into a single list.
     *
     * @param projectDTO The ProjectDTO containing categorized compilation units.
     * @return A list of all CustomCompilationUnitDTOs in the project.
     */
    private List<CustomCompilationUnitDTO> extractCompilationUnits(ProjectDTO projectDTO) {
        return Stream.of(
                projectDTO.getEntities(),
                projectDTO.getRepositories(),
                projectDTO.getServices(),
                projectDTO.getControllers(),
                projectDTO.getDocuments(),
                projectDTO.getTestClasses()
        ).flatMap(List::stream).toList();
    }

    /**
     * Extracts all compilation units from a ProjectDTO into a single list.
     *
     * @param projectDTO The ProjectDTO containing categorized compilation units.
     * @return A list of all CustomCompilationUnitDTOs in the project.
     */
    private List<CustomCompilationUnitDTO> extractCompilationUnitsWithoutTests(ProjectDTO projectDTO) {
        return Stream.of(
                projectDTO.getEntities(),
                projectDTO.getRepositories(),
                projectDTO.getServices(),
                projectDTO.getControllers(),
                projectDTO.getDocuments()
        ).flatMap(List::stream).toList();
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
     * Creates a CompUnitWithAnalysisDTO containing the given compilation unit and its analysis.
     *
     * @param compilationUnit The CustomCompilationUnitDTO to be analyzed.
     * @param internalBasePackage The internal base package as a String.
     * @param pomFileDTO The PomFileDTO containing the parsed information from the pom.xml file.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in the analysis.
     * @return A CompUnitWithAnalysisDTO containing the compilation unit and its analysis.
     */
    private CompUnitWithAnalysisDTO createAnalysisDTO(
            CustomCompilationUnitDTO compilationUnit,
            List<CustomCompilationUnitDTO> projectCompUnitsWithoutTests,
            String internalBasePackage,
            PomFileDTO pomFileDTO,
            boolean includeNonInternalDependencies
    ) {
        AnalysedCompUnitDTO analysis = summarizingService.analyseCompUnit(
                compilationUnit,
                projectCompUnitsWithoutTests,
                internalBasePackage,
                pomFileDTO,
                includeNonInternalDependencies
        );
        CompUnitWithAnalysisDTO compUnitWithAnalysis = new CompUnitWithAnalysisDTO();
        compUnitWithAnalysis.setCompilationUnit(compilationUnit);
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
        return projectAnalysisDTO;
    }

    private List<CompUnitWithAnalysisDTO> filterAnalysedUnitByAnnotation(List<CompUnitWithAnalysisDTO> units, AnnotationType annotationType) {
        return units.stream()
                .filter(dto -> dto.getCompilationUnit().getAnnotations().stream()
                        .anyMatch(annotation -> annotation.getName().equalsIgnoreCase(annotationType.getAnnotation())))
                .toList();
    }
}