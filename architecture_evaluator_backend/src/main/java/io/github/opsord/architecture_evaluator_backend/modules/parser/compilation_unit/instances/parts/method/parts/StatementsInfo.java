package io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts;

import io.github.opsord.architecture_evaluator_backend.modules.parser.compilation_unit.instances.parts.method.parts.statement.StatementInstance;
import lombok.Data;

import java.util.List;

@Data
public class StatementsInfo {
    private int numberOfStatements;
    private int numberOfExecutableStatements;
    private int numberOfControlStatements;
    private List<StatementInstance> statements;
}
