package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.AnalysedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.CompUnitWithAnalysisDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.ProjectAnalysisDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.CCUSummarizingService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.LayerAnnotation;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorService.class);

    private final PomService pomService;
    private final ProjectService projectService;
    private final CCUSummarizingService summarizingService;

    public ProjectAnalysisDTO orchestrateProjectAnalysis(String projectPath, boolean includeNonInternalDependencies) throws IOException {
        logger.info("Starting orchestration for project at path: {}", projectPath);

        ProjectDTO projectDTO = scanProject(projectPath);
        PomFileDTO pomFileDTO = scanPomFile(projectPath);

        String internalBasePackage = determineInternalBasePackage(pomFileDTO);

        List<CompUnitWithAnalysisDTO> analyzedUnits = analyzeCompilationUnits(
                projectDTO,
                internalBasePackage,
                pomFileDTO,
                includeNonInternalDependencies
        );

        ProjectAnalysisDTO projectAnalysisDTO = organizeProjectAnalysis(analyzedUnits);
        projectAnalysisDTO.setProjectPath(projectPath);
        projectAnalysisDTO.setPomFile(pomFileDTO);

        logger.info("Orchestration completed for project at path: {}", projectPath);
        return projectAnalysisDTO;
    }

    private ProjectDTO scanProject(String projectPath) throws IOException {
        return projectService.scanProjectToDTO(projectPath);
    }

    private PomFileDTO scanPomFile(String projectPath) {
        PomFileDTO pomFileDTO = pomService.scanPomFile(projectPath);
        if (pomFileDTO == null) {
            throw new IllegalStateException("Failed to scan pom.xml file at path: " + projectPath);
        }
        return pomFileDTO;
    }

    private String determineInternalBasePackage(PomFileDTO pomFileDTO) {
        return pomFileDTO.getGroupId();
    }

    private List<CompUnitWithAnalysisDTO> analyzeCompilationUnits(
            ProjectDTO projectDTO,
            String internalBasePackage,
            PomFileDTO pomFileDTO,
            boolean includeNonInternalDependencies
    ) {
        List<CustomCompilationUnitDTO> allUnits = Stream.of(
                projectDTO.getEntities(),
                projectDTO.getRepositories(),
                projectDTO.getServices(),
                projectDTO.getControllers(),
                projectDTO.getDocuments(),
                projectDTO.getTestClasses()
        ).flatMap(List::stream).toList();

        return allUnits.stream()
                .map(compilationUnit -> createAnalysisDTO(
                        compilationUnit,
                        allUnits,
                        internalBasePackage,
                        pomFileDTO,
                        includeNonInternalDependencies
                ))
                .toList();
    }

    private CompUnitWithAnalysisDTO createAnalysisDTO(
            CustomCompilationUnitDTO compilationUnit,
            List<CustomCompilationUnitDTO> allEntities,
            String internalBasePackage,
            PomFileDTO pomFileDTO,
            boolean includeNonInternalDependencies
    ) {
        AnalysedCompUnitDTO analysis = summarizingService.analyseCompUnit(
                compilationUnit,
                allEntities,
                internalBasePackage,
                pomFileDTO,
                includeNonInternalDependencies
        );
        CompUnitWithAnalysisDTO compUnitWithAnalysis = new CompUnitWithAnalysisDTO();
        compUnitWithAnalysis.setCompilationUnit(compilationUnit);
        compUnitWithAnalysis.setAnalysis(analysis);
        return compUnitWithAnalysis;
    }

    public ProjectAnalysisDTO organizeProjectAnalysis(List<CompUnitWithAnalysisDTO> compUnitWithAnalysisDTOS) {
        ProjectAnalysisDTO projectAnalysisDTO = new ProjectAnalysisDTO();
        projectAnalysisDTO.setEntities(filterByLayer(compUnitWithAnalysisDTOS, "entity"));
        projectAnalysisDTO.setDocuments(filterByLayer(compUnitWithAnalysisDTOS, "document"));
        projectAnalysisDTO.setRepositories(filterByLayer(compUnitWithAnalysisDTOS, "repository"));
        projectAnalysisDTO.setServices(filterByLayer(compUnitWithAnalysisDTOS, "service"));
        projectAnalysisDTO.setControllers(filterByLayer(compUnitWithAnalysisDTOS, "controller"));
        return projectAnalysisDTO;
    }

    private List<CompUnitWithAnalysisDTO> filterByLayer(List<CompUnitWithAnalysisDTO> units, String layer) {
        return units.stream()
                .filter(dto -> dto.getCompilationUnit().getAnnotations().stream()
                        .anyMatch(annotation -> {
                            try {
                                return LayerAnnotation.valueOf(layer.toUpperCase()).getAnnotation()
                                        .equalsIgnoreCase(annotation.getName());
                            } catch (IllegalArgumentException e) {
                                return false;
                            }
                        }))
                .collect(Collectors.toList());
    }
}