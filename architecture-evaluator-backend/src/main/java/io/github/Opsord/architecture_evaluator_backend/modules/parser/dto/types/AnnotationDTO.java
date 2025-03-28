// AnnotationDTO.java
package io.github.Opsord.architecture_evaluator_backend.modules.parser.dto.types;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AnnotationDTO {
    private String name;
    private Map<String, String> attributes;
}