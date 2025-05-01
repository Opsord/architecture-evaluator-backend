package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.statement;

import lombok.Getter;

@Getter
public enum StatementType {
    IF("IfStmt"),
    FOR("ForStmt"),
    WHILE("WhileStmt"),
    SWITCH("SwitchStmt"),
    EXPRESSION("ExpressionStmt"),
    RETURN("ReturnStmt"),
    THROW("ThrowStmt"),
    TRY("TryStmt");

    private final String type;

    StatementType(String type) {
        this.type = type;
    }

    public static StatementType fromString(String type) {
        for (StatementType statementType : StatementType.values()) {
            if (statementType.getType().equalsIgnoreCase(type)) {
                return statementType;
            }
        }
        throw new IllegalArgumentException("Unknown statement type: " + type);
    }
}
