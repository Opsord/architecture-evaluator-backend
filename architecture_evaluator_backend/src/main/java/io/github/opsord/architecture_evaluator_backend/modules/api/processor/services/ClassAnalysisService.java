package io.github.opsord.architecture_evaluator_backend.modules.api.processor.services;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.ClassAnalysisInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts.*;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts.*;
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
public class ClassAnalysisService {

    private final ProgramMetricsService programMetricsService;
    private final ComplexityMetricsService complexityMetricsService;
    private final CouplingMetricsService couplingMetricsService;
    private final CohesionMetricsService cohesionMetricsService;

    /**
     * Analyzes a {@link ClassInstance} and generates a {@link ClassAnalysisInstance}
     * containing metrics and dependency classifications.
     *
     * @param classInstance          The class to analyze.
     * @param classifiedDependencies Map of import categories to dependency names.
     * @return The analysis result for the given class.
     */
    public ClassAnalysisInstance analyseClassInstance(
            ClassInstance classInstance,
            Map<ImportCategory, List<String>> classifiedDependencies) {

        ClassAnalysisInstance classAnalysisInstance = new ClassAnalysisInstance();
        // Metrics for this class
        classAnalysisInstance.setProgramMetrics(programMetricsService.generateProgramMetrics(classInstance));
        classAnalysisInstance.setComplexityMetrics(complexityMetricsService.calculateComplexityMetrics(classInstance));
        classAnalysisInstance.setCouplingMetrics(couplingMetricsService.calculateCouplingMetrics(classInstance));
        classAnalysisInstance.setCohesionMetrics(cohesionMetricsService.calculateCohesionMetrics(classInstance));
        classAnalysisInstance.setClassifiedDependencies(classifiedDependencies);
        return classAnalysisInstance;
    }
}