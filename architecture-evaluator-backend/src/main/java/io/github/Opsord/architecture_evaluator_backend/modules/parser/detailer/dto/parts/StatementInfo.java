package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatementInfo {
    private int numberOfStatements;
    private int numberOfExecutableStatements;
    private int numberOfControlStatements;
}