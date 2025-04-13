package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatementDTO {
    private String type;
    private String structure;
    private String name;
}
