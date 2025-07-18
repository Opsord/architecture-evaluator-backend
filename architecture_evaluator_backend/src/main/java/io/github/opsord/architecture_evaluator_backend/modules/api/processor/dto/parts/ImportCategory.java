package io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts;

import lombok.Getter;

@Getter
public enum ImportCategory {
    PARENT_DEPENDENCY,
    JAVA_STANDARD,
    SPRING,
    INTERNAL,
    EXTERNAL_KNOWN,
    EXTERNAL_UNKNOWN
}
