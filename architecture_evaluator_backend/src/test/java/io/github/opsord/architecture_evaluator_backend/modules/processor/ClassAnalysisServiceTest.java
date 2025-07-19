package io.github.opsord.architecture_evaluator_backend.modules.processor;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.ClassAnalysisInstance;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts.*;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.ClassAnalysisService;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts.CohesionMetricsService;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts.ComplexityMetricsService;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts.CouplingMetricsService;
import io.github.opsord.architecture_evaluator_backend.modules.api.processor.services.parts.ProgramMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClassAnalysisServiceTest {

    private ProgramMetricsService programMetricsService;
    private ComplexityMetricsService complexityMetricsService;
    private CouplingMetricsService couplingMetricsService;
    private CohesionMetricsService cohesionMetricsService;
    private ClassAnalysisService classAnalysisService;

    @BeforeEach
    void setUp() {
        programMetricsService = mock(ProgramMetricsService.class);
        complexityMetricsService = mock(ComplexityMetricsService.class);
        couplingMetricsService = mock(CouplingMetricsService.class);
        cohesionMetricsService = mock(CohesionMetricsService.class);

        classAnalysisService = new ClassAnalysisService(
                programMetricsService,
                complexityMetricsService,
                couplingMetricsService,
                cohesionMetricsService
        );
    }

    @Test
    void testAnalyseClassInstance_setsAllMetricsAndDependencies() {
        ClassInstance classInstance = new ClassInstance();
        Map<ImportCategory, List<String>> classifiedDependencies = Map.of(
                ImportCategory.JAVA_STANDARD, List.of("java.util.List")
        );

        ProgramMetricsDTO programMetrics = new ProgramMetricsDTO();
        ComplexityMetricsDTO complexityMetrics = new ComplexityMetricsDTO();
        CouplingMetricsDTO couplingMetrics = new CouplingMetricsDTO();
        CohesionMetricsDTO cohesionMetrics = new CohesionMetricsDTO();

        when(programMetricsService.generateProgramMetrics(classInstance)).thenReturn(programMetrics);
        when(complexityMetricsService.calculateComplexityMetrics(classInstance)).thenReturn(complexityMetrics);
        when(couplingMetricsService.calculateCouplingMetrics(classInstance)).thenReturn(couplingMetrics);
        when(cohesionMetricsService.calculateCohesionMetrics(classInstance)).thenReturn(cohesionMetrics);

        ClassAnalysisInstance result = classAnalysisService.analyseClassInstance(classInstance, classifiedDependencies);

        assertSame(programMetrics, result.getProgramMetrics());
        assertSame(complexityMetrics, result.getComplexityMetrics());
        assertSame(couplingMetrics, result.getCouplingMetrics());
        assertSame(cohesionMetrics, result.getCohesionMetrics());
        assertEquals(classifiedDependencies, result.getClassifiedDependencies());
    }
}