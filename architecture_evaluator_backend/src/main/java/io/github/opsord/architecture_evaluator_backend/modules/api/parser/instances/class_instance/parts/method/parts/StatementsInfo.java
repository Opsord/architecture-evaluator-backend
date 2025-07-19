package io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts;

import io.github.opsord.architecture_evaluator_backend.modules.api.parser.instances.class_instance.parts.method.parts.statement.StatementInstance;
import lombok.Data;

import java.util.List;

@Data
public class StatementsInfo {
    private int numberOfStatements;              // all statements
    private int numberOfExecutableStatements;    // EXPRESSION, RETURN, THROW ...
    private int numberOfControlStatements;       // IF, FOR, WHILE, SWITCH, TRY, CATCH
    private int numberOfReturnStatements;        // RETURN
    private int numberOfThrowStatements;         // THROW
    private int numberOfCatchClauses;            // CATCH
    private int numberOfLogicalOperators;        // "&&" or "||"
    private int numberOfTernaryOperators;        // ?:

    private List<StatementInstance> statements;  // raw AST fragments
}
