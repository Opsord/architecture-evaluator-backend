package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.analysis.parts;

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
