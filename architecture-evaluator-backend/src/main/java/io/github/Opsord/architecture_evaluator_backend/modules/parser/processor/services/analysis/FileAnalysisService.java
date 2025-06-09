package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.analysis;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.CustomCompilationUnit;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_instance.FileInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.AnalysedClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.parts.*;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.analysis.parts.*;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.project_scanner.dto.pom.PomFileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FileAnalysisService {

    private final ProgramMetricsService programMetricsService;
    private final ComplexityMetricsService complexityMetricsService;
    private final CouplingMetricsService couplingMetricsService;
    private final CohesionMetricsService cohesionMetricsService;
    private final ImportClassifierService importClassifierService;

    /**
     * Analyzes a compilation unit and generates a detailed analysis.
     *
     * @param fileInstance The compilation unit to analyze.
     * @param projectCompUnitsWithoutTests The list of project compilation units excluding test classes.
     * @param internalBasePackage The internal base package for dependency classification.
     * @param pomFileDTO The POM file containing project dependencies.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in the analysis.
     * @return An `AnalysedCompUnitDTO` containing the detailed analysis.
     */
    public AnalysedClassInstance analyseCompUnit(FileInstance fileInstance,
                                                 List<FileInstance> projectCompUnitsWithoutTests,
                                                 String internalBasePackage,
                                                 PomFileDTO pomFileDTO,
                                                 boolean includeNonInternalDependencies) {
        AnalysedClassInstance detailedCompUnit = new AnalysedClassInstance();

        // Classify dependencies
        Map<ImportCategory, List<String>> classifiedDependencies = classifyDependencies(
                fileInstance, pomFileDTO, internalBasePackage);
        detailedCompUnit.setClassifiedDependencies(classifiedDependencies);

        // Set basic metrics
        setBasicMetrics(detailedCompUnit, fileInstance);

        // Generate and set program metrics
        ProgramMetricsDTO programMetrics = programMetricsService.generateProgramMetrics(fileInstance);
        detailedCompUnit.setProgramMetrics(programMetrics);

        // Generate and set complexity metrics
        ComplexityMetricsDTO complexityMetrics = complexityMetricsService.calculateComplexityMetrics(
                fileInstance);
        detailedCompUnit.setComplexityMetrics(complexityMetrics);

        // Generate and set coupling metrics
        CouplingMetricsDTO couplingMetrics = couplingMetricsService.calculateCouplingMetrics(
                fileInstance, projectCompUnitsWithoutTests, classifiedDependencies, includeNonInternalDependencies);
        detailedCompUnit.setCouplingMetrics(couplingMetrics);

        // Generate and set cohesion metrics
        CohesionMetricsDTO cohesionMetrics = cohesionMetricsService.calculateCohesionMetrics(fileInstance);
        detailedCompUnit.setCohesionMetrics(cohesionMetrics);

        return detailedCompUnit;
    }

    // -------------------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------------------

    private Map<ImportCategory, List<String>> classifyDependencies(CustomCompilationUnit compilationUnitDTO,
                                                                   PomFileDTO pomFileDTO,
                                                                   String internalBasePackage) {
        return importClassifierService.classifyDependencies(pomFileDTO, compilationUnitDTO, internalBasePackage);
    }

    private void setBasicMetrics(AnalysedClassInstance detailedCompUnit, CustomCompilationUnit compilationUnitDTO) {
        detailedCompUnit.setClassCount(compilationUnitDTO.getClasses().size());
        detailedCompUnit.setInterfaceCount(compilationUnitDTO.getInterfaceNames().size());
        detailedCompUnit.setStatementCount(compilationUnitDTO.getStatements().size());
    }
}