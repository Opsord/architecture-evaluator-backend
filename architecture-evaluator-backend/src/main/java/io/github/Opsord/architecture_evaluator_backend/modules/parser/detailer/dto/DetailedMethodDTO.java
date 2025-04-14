package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailedMethodDTO {
    private String name;
    private String accessModifier;
    private String returnType;
    private int statementCount;
    private int controlStatementCount;
    private int approximateCC;
}
