package io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts;

import lombok.Data;

@Data
public class MethodMetrics {
    private Integer linesOfCode;
    private Integer mcCabeComplexity;
}