package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.summary.parts;

import lombok.Data;

import java.util.List;

@Data
public class MethodSummaryDTO {
    private String methodName;
    private List<String> usedVariables;
    private String returnType;
    private Integer linesOfCode;
}
