package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts;

import lombok.Data;

@Data
public class VariableDTO {
    private String name;
    private String type;
    private String scope; // "instance" or "local"
}