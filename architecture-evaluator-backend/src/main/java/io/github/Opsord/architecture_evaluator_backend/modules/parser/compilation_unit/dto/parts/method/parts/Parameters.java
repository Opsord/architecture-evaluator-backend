package io.github.Opsord.architecture_evaluator_backend.modules.parser.compilation_unit.dto.parts.method.parts;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Parameters {
    private List<String> inputs;
    private List<String> outputs;
}
