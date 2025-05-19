package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.statement.StatementDTO;
import lombok.Data;

import java.util.List;

@Data
public class StatementsInfo {
    private int numberOfStatements;
    private int numberOfExecutableStatements;
    private int numberOfControlStatements;
    private List<StatementDTO> statements;
}
