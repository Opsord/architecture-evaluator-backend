package io.github.Opsord.architecture_evaluator_backend.modules.parser.detailer.dto.method.parts;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BasicInfo {
    private String name;
    private String accessModifier;
    private String returnType;
}
