package io.github.Opsord.architecture_evaluator_backend.modules.parser.processor.dto.parts;

import lombok.Data;

@Data
public class ProgramMetricsDTO {

    private int numberOfMethods;
    private int sumOfExecutableStatements;
    private int maxInputParameters;
    private int maxOutputParameters;
    private int linesOfCode;
}
