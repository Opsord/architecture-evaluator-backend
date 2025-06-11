package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.parts;

import lombok.Data;

@Data
public class MethodMetrics {
    private Integer linesOfCode;
    private Integer mcCabeComplexity;
}