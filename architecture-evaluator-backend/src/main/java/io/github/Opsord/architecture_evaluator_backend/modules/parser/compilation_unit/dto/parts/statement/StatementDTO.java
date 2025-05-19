package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.statement;

import lombok.Data;

@Data
public class StatementDTO {
    private StatementType type;
    private String structure;
}
