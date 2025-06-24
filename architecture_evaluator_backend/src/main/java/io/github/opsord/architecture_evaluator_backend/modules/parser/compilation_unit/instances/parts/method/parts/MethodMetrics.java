package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts;

import lombok.Data;

@Data
public class MethodMetrics {
    private Integer linesOfCode;
    private Integer mcCabeComplexity;
}