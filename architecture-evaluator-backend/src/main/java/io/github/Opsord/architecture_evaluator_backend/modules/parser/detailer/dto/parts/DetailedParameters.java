package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.parts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DetailedParameters {
    private int numberOfInputs;
    private int numberOfOutputs;
    private int linesOfCode;
}
