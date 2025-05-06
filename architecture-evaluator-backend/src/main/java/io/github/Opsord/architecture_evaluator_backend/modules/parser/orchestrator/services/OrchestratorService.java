package io.github.Opsord.architecture_evaluator_backend.modules.parser.orchestrator.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_manager.dto.ProjectDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_manager.services.ProjectScannerService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.CustomCompUnitDetailingService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.DetailedCompUnitDTO;
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

    private final ProjectScannerService projectScannerService;
    private final CustomCompUnitDetailingService detailingService;

    public List<DetailedCompUnitDTO> orchestrateProjectAnalysis(String projectPath) throws IOException {
        logger.info("Starting orchestration for project at path: {}", projectPath);

        // Step 1: Scan and organize the project
        ProjectDTO projectDTO = projectScannerService.scanAndOrganizeProject(projectPath);

        // Step 2: Parse and detail each compilation unit
        List<CustomCompilationUnitDTO> allUnits = collectAllCompilationUnits(projectDTO);
        return allUnits.stream()
                .map(detailingService::generateDetailedCompUnit)
                .collect(Collectors.toList());
    }

    private List<CustomCompilationUnitDTO> collectAllCompilationUnits(ProjectDTO projectDTO) {
        return Stream.of(
                projectDTO.getEntities(),
                projectDTO.getRepositories(),
                projectDTO.getServices(),
                projectDTO.getControllers(),
                projectDTO.getDocuments()
        ).flatMap(List::stream).collect(Collectors.toList());
    }
}