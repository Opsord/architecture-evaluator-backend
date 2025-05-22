package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CohesionMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.CouplingMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.ProgramMetricsDTO;
import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.ImportCategory;
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
    private CouplingMetricsDTO couplingMetrics;
    private CohesionMetricsDTO cohesionMetrics;
}
