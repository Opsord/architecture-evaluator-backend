package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts.ProgramMetricsDTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysedCompUnitDTO {
    private int classCount;
    private int interfaceCount;
    private int statementCount;
    private ProgramMetricsDTO programMetrics;
}
