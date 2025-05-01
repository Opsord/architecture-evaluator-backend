package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProgramMetricsDTO {

    private int numberOfMethods;
    private int sumOfExecutableStatements;
    private int maxInputParameters;
    private int maxOutputParameters;
    private int linesOfCode;

    private Number improvedCyclomaticComplexity;
}
