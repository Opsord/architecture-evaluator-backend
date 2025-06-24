package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances;

import lombok.Getter;

@Getter
public enum JavaFileType {
    CLASS,
    INTERFACE,
    ENUM,
    RECORD,
    ANNOTATION,
    EXCEPTION,
    ABSTRACT_CLASS,
    UNKNOWN;

    public boolean isClassType() {
        return this == CLASS || this == ABSTRACT_CLASS;
    }
}
