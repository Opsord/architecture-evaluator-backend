package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.parts.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AnalysedClassInstance {
    private int classCount;
    private int interfaceCount;
    private int statementCount;
    private Map<ImportCategory, List<String>> classifiedDependencies;
    private ProgramMetricsDTO programMetrics;
    private ComplexityMetricsDTO complexityMetrics;
    private CouplingMetricsDTO couplingMetrics;
    private CohesionMetricsDTO cohesionMetrics;
}
