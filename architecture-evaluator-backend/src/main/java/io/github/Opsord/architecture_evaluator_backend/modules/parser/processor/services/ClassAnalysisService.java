package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.ClassAnalysis;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts.*;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.services.parts.*;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Service for analyzing a class and generating metrics and dependency classifications.
 */
@Service
@RequiredArgsConstructor
public class ClassAnalysisService {

    private final ProgramMetricsService programMetricsService;
    private final ComplexityMetricsService complexityMetricsService;
    private final CouplingMetricsService couplingMetricsService;
    private final CohesionMetricsService cohesionMetricsService;

    /**
     * Analyzes a {@link ClassInstance} and generates a {@link ClassAnalysis} containing metrics and dependency classifications.
     *
     * @param classInstance The class to analyze.
     * @param allProjectClasses List of all classes in the project.
     * @param classifiedDependencies Map of import categories to dependency names.
     * @param includeNonInternalDependencies Whether to include non-internal dependencies in coupling metrics.
     * @return The analysis result for the given class.
     */
    public ClassAnalysis analyseClassInstance(
            ClassInstance classInstance,
            List<ClassInstance> allProjectClasses,
            Map<ImportCategory, List<String>> classifiedDependencies,
            boolean includeNonInternalDependencies
    ) {
        ClassAnalysis classAnalysis = new ClassAnalysis();

        // Metrics for this class
        classAnalysis.setProgramMetrics(programMetricsService.generateProgramMetrics(classInstance));
        classAnalysis.setComplexityMetrics(complexityMetricsService.calculateComplexityMetrics(classInstance));
        classAnalysis.setCouplingMetrics(
                couplingMetricsService.calculateCouplingMetrics(
                        classInstance,
                        allProjectClasses,
                        classifiedDependencies,
                        includeNonInternalDependencies
                )
        );
        classAnalysis.setCohesionMetrics(cohesionMetricsService.calculateCohesionMetrics(classInstance));
        classAnalysis.setClassifiedDependencies(classifiedDependencies);

        return classAnalysis;
    }
}