package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.CustomCompilationUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.AnalysedCompUnitDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CohesionMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CouplingMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.ImportCategory;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.ProgramMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts.CohesionMetricsService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts.CouplingMetricsService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts.ImportClassifierService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.services.parts.ProgramMetricsService;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CCUSummarizingService {

    private final ProgramMetricsService programMetricsService;
    private final CouplingMetricsService couplingMetricsService;
    private final CohesionMetricsService cohesionMetricsService;
    private final ImportClassifierService importClassifierService;

    /**
     * Analyzes a compilation unit and generates a detailed analysis.
     *
     * @param compilationUnitDTO The compilation unit to analyze.
     * @param projectCompUnitsWithoutTests The list of project compilation units excluding test classes.
     * @param internalBasePackage The internal base package for dependency classification.
     * @param pomFileDTO The POM file containing project dependencies.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in the analysis.
     * @return An `AnalysedCompUnitDTO` containing the detailed analysis.
     */
    public AnalysedCompUnitDTO analyseCompUnit(CustomCompilationUnitDTO compilationUnitDTO,
                                               List<CustomCompilationUnitDTO> projectCompUnitsWithoutTests,
                                               String internalBasePackage,
                                               PomFileDTO pomFileDTO,
                                               boolean includeNonInternalDependencies) {
        AnalysedCompUnitDTO detailedCompUnit = new AnalysedCompUnitDTO();

        // Classify dependencies
        Map<ImportCategory, List<String>> classifiedDependencies = classifyDependencies(
                compilationUnitDTO, pomFileDTO, internalBasePackage);
        detailedCompUnit.setClassifiedDependencies(classifiedDependencies);

        // Set basic metrics
        setBasicMetrics(detailedCompUnit, compilationUnitDTO);

        // Generate and set program metrics
        ProgramMetricsDTO programMetrics = programMetricsService.generateProgramMetrics(compilationUnitDTO);
        detailedCompUnit.setProgramMetrics(programMetrics);

        // Generate and set coupling metrics
        CouplingMetricsDTO couplingMetrics = couplingMetricsService.calculateCouplingMetrics(
                compilationUnitDTO, projectCompUnitsWithoutTests, classifiedDependencies, includeNonInternalDependencies);
        detailedCompUnit.setCouplingMetrics(couplingMetrics);

        // Generate and set cohesion metrics
        CohesionMetricsDTO cohesionMetrics = cohesionMetricsService.calculateCohesionMetrics(compilationUnitDTO);
        detailedCompUnit.setCohesionMetrics(cohesionMetrics);

        return detailedCompUnit;
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private Map<ImportCategory, List<String>> classifyDependencies(CustomCompilationUnitDTO compilationUnitDTO,
                                                                   PomFileDTO pomFileDTO,
                                                                   String internalBasePackage) {
        return importClassifierService.classifyDependencies(pomFileDTO, compilationUnitDTO, internalBasePackage);
    }

    private void setBasicMetrics(AnalysedCompUnitDTO detailedCompUnit, CustomCompilationUnitDTO compilationUnitDTO) {
        detailedCompUnit.setClassCount(compilationUnitDTO.getClassName().size());
        detailedCompUnit.setInterfaceCount(compilationUnitDTO.getInterfaceNames().size());
        detailedCompUnit.setStatementCount(compilationUnitDTO.getStatements().size());
    }
}