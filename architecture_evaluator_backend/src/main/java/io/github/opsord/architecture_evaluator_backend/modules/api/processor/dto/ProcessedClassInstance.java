package io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.ClassInstance;
import lombok.Data;

@Data
public class ProcessedClassInstance {
    private ClassInstance classInstance;
    private ClassAnalysisInstance classAnalysisInstance;
}
