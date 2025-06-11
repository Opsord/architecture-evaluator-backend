package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.ClassInstance;
import lombok.Data;

@Data
public class ProcessedClassInstance {
    private ClassInstance classInstance;
    private ClassAnalysis classAnalysis;
}
