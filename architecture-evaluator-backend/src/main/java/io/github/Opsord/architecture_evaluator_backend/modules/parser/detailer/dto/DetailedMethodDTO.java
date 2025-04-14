// DetailedMethodDTO.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailedMethodDTO {
    // Basic information
    private String name;
    private String accessModifier;
    private String returnType;

    // Parameters to calculate CC
    private int approximateCC;
    private int numberOfStatements;
    private int numberOfControlStatements;

    // Parameters to calculate ICC
    private int improvedCC;
    private int numberOfInputs;
    private int numberOfOutputs;
    private int linesOfCode;
}