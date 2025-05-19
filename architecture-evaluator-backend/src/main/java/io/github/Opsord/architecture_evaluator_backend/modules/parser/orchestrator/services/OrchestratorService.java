package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.AnalysedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.CompUnitWithAnalysisDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.dto.ProjectAnalysisDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.CCUSummarizingService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.LayerAnnotation;
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

@Service
@RequiredArgsConstructor
public class OrchestratorService {

    private static final Logger logger = LoggerFactory.getLogger(OrchestratorService.class);

    private final PomService pomService;
    private final ProjectService projectService;
    private final CCUSummarizingService summarizingService;

    public ProjectAnalysisDTO orchestrateProjectAnalysis(String projectPath, boolean includeNonInternalDependencies) throws IOException {
        logger.info("Starting orchestration for project at path: {}", projectPath);

        // Scan and parse the compilation units in the project
        List<CustomCompilationUnitDTO> compilationUnitDTOS = projectService.scanProject(projectPath);

        // Scan the pom.xml file
        PomFileDTO pomFileDTO = pomService.scanPomFile(projectPath);
        if (pomFileDTO == null) {
            throw new IllegalStateException("Failed to scan pom.xml file at path: " + projectPath);
        }

        // Define the internal base package (e.g., from the groupId in pom.xml)
        String internalBasePackage = pomFileDTO.getGroupId();

        // Analyze the compilation units and generate CompUnitWithAnalysisDTO
        List<CompUnitWithAnalysisDTO> compUnitWithAnalysisDTOS = compilationUnitDTOS.stream()
                .map(compilationUnit -> {
                    AnalysedCompUnitDTO analysis = summarizingService.analyseCompUnit(
                            compilationUnit,
                            compilationUnitDTOS,
                            internalBasePackage,
                            pomFileDTO,
                            includeNonInternalDependencies
                    );
                    CompUnitWithAnalysisDTO compUnitWithAnalysis = new CompUnitWithAnalysisDTO();
                    compUnitWithAnalysis.setCompilationUnit(compilationUnit);
                    compUnitWithAnalysis.setAnalysis(analysis);
                    return compUnitWithAnalysis;
                })
                .toList();

        // Organize the analyzed units into a ProjectAnalysisDTO
        ProjectAnalysisDTO projectAnalysisDTO = organizeProjectAnalysis(compUnitWithAnalysisDTOS);
        projectAnalysisDTO.setProjectPath(projectPath);
        projectAnalysisDTO.setPomFile(pomFileDTO);

        logger.info("Orchestration completed for project at path: {}", projectPath);
        return projectAnalysisDTO;
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

    /**
     * Filters the list of CompilationUnitAnalysisDTO by the specified layer.
     *
     * @param units List of CompilationUnitAnalysisDTO to filter.
     * @param layer Layer to filter by (e.g., "entity", "repository", "service", "controller").
     * @return List of CompilationUnitAnalysisDTO filtered by the specified layer.
     */
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