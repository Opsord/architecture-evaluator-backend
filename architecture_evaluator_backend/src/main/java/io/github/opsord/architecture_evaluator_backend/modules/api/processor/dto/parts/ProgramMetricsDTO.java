package io.github.opsord.architecture_evaluator_backend.modules.api.processor.dto.parts;

import lombok.Data;

@Data
public class ProgramMetricsDTO {

    private int numberOfMethods;
    private int sumOfExecutableStatements;
    private int maxInputParameters;
    private int maxOutputParameters;
    private int linesOfCode;
}
