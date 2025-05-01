package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.MethodDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DetailedCompUnitDTO {
    private int classCount;
    private List<MethodDTO> methods;
    private int numberOfMethods;
    private int linesOfCode;
    private ProgramMetricsDTO programMetrics;
}
