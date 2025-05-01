package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.statement;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatementDTO {
    private StatementType type;
    private String structure;
}
