package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.*;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AnalysedCompUnitDTO {
    private int classCount;
    private int interfaceCount;
    private int statementCount;

    private Map<ImportCategory, List<String>> classifiedDependencies;

    private ProgramMetricsDTO programMetrics;
    private ComplexityMetricsDTO complexityMetrics;
    private CouplingMetricsDTO couplingMetrics;
    private CohesionMetricsDTO cohesionMetrics;
}
