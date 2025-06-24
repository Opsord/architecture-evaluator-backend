package io.github.opsord.architecture_evaluator_backend.modules.parser.processor.dto;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.JavaTypeInstance;
import lombok.Data;

@Data
public class ProcessedJavaTypeInstance {
    private JavaTypeInstance classInstance;
    private JavaTypeAnalysis javaTypeAnalysis;
}
