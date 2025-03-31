// MethodDTO.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.dto.parts;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MethodDTO {
    private String name;
    private String accessModifier;
    private String returnType;
    private List<ParameterDTO> parameters;
}