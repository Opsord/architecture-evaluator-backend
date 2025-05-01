package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts;

import io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.statement.StatementDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StatementsInfo {
    private int numberOfStatements;
    private int numberOfExecutableStatements;
    private int numberOfControlStatements;
    private List<StatementDTO> statements;
}
