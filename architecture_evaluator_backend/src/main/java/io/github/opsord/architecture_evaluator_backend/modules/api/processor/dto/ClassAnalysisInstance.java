package io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto;

import io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ClassAnalysisInstance {
    private int classCount;
    private int interfaceCount;
    private int statementCount;
    private Map<ImportCategory, List<String>> classifiedDependencies;
    private ProgramMetricsDTO programMetrics;
    private ComplexityMetricsDTO complexityMetrics; // Generated from ClassInstance
    private CouplingMetricsDTO couplingMetrics;
    private CohesionMetricsDTO cohesionMetrics; // Generated from ClassInstance
}
