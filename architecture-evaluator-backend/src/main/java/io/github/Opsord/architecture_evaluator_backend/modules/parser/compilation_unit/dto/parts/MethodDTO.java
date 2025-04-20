// MethodDTO.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MethodDTO {
    // Basic information
    private String name;
    private String accessModifier;
    private String returnType;

    // Statements
    private List<StatementDTO> statements;
    private List<StatementDTO> executableStatements;
    private List<StatementDTO> controlStatements;

    // Parameters to calculate ICC
    private List<String> inputs;
    private List<String> outputs;
    private int linesOfCode;
}