package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodMetrics {
    private int linesOfCode;
    private int approxMcCabeCC;
}