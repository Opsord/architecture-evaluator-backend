package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.class_instance.parts.method.parts.statement;

import lombok.Getter;

@Getter
public enum StatementType {
    IF,
    FOR,
    WHILE,
    SWITCH,
    EXPRESSION,
    RETURN,
    THROW,
    TRY
}
