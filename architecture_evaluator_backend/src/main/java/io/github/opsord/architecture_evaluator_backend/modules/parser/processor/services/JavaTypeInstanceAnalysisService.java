package io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaTypeInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.file_types.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.JavaTypeAnalysis;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.*;
import io.github.opsord.architecture_evaluator_backend.modules.parser.processor.services.parts.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Service for analyzing a class and generating metrics and dependency
 * classifications.
 */
@Service
@RequiredArgsConstructor
public class JavaTypeInstanceAnalysisService {

    private final ProgramMetricsService programMetricsService;
    private final ComplexityMetricsService complexityMetricsService;
    private final CouplingMetricsService couplingMetricsService;
    private final CohesionMetricsService cohesionMetricsService;

    /**
     * Analyzes a {@link ClassInstance} and generates a {@link JavaTypeAnalysis}
     * containing metrics and dependency classifications.
     *
     * @param javaTypeInstance The java-type instance to analyze.
     * @param classifiedDependencies Map of import categories to dependency names.
     * @return The analysis result for the given class.
     */
    public JavaTypeAnalysis analyseClassInstance(
            JavaTypeInstance javaTypeInstance,
            Map<ImportCategory, List<String>> classifiedDependencies) {

        JavaTypeAnalysis javaTypeAnalysis = new JavaTypeAnalysis();
        // Metrics for this class
        javaTypeAnalysis.setProgramMetrics(programMetricsService.generateProgramMetrics(javaTypeInstance));
        javaTypeAnalysis.setComplexityMetrics(complexityMetricsService.calculateComplexityMetrics(javaTypeInstance));
        javaTypeAnalysis.setCouplingMetrics(couplingMetricsService.calculateCouplingMetrics(javaTypeInstance));
        javaTypeAnalysis.setCohesionMetrics(cohesionMetricsService.calculateCohesionMetrics(javaTypeInstance));
        javaTypeAnalysis.setClassifiedDependencies(classifiedDependencies);
        return javaTypeAnalysis;
    }
}