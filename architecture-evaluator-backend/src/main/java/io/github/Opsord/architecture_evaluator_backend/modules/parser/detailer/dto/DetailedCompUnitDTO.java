package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailedCompUnitDTO {
    private int classCount;
    private int interfaceCount;
    private int statementCount;
    private ProgramMetricsDTO programMetrics;
}
