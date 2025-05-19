package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts;

import lombok.Data;

import java.util.List;

@Data
public class Parameters {
    private List<String> inputs;
    private List<String> outputs;
}
